package dom_2;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class FileIndexerAppBarrier {    
    
    private static final int QUEUE_CAPACITY = 100;
    private static final int NUM_CONSUMERS = Runtime.getRuntime().availableProcessors();
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB
    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(".java"));
    private static final long STATS_INTERVAL_MS = 1000;
    
    // Timeout za poll operaciju (ms) - consumer ce cekati ovoliko pre nego sto proveri latch
    private static final long POLL_TIMEOUT_MS = 100;
    
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Upotreba: java FileIndexerAppBarrier <putanja_direktorijuma>");
            System.exit(1);
        }
        
        File rootDir = new File(args[0]);
        
        if (!rootDir.exists() || !rootDir.isDirectory()) {
            System.out.println("Greska: Nevalidan direktorijum: " + args[0]);
            System.exit(1);
        }       
        System.out.println("Koreni direktorijum: " + rootDir.getAbsolutePath());
        
        startIndexingWithBarrier(rootDir);
    }
    
    public static void startIndexingWithBarrier(File rootDir) {
        long startTime = System.currentTimeMillis();
        
        // Deljene strukture podataka
        BlockingQueue<File> queue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);
        ConcurrentHashMap<String, FileInfo> indexMap = new ConcurrentHashMap<>();
        Statistics stats = new Statistics();
        
        // Odredjivanje broja producera
        File[] rootEntries = rootDir.listFiles();
        int numProducers = 0;
        List<File> producerRoots = new ArrayList<>();
        
        if (rootEntries != null) {
            for (File entry : rootEntries) {
                if (entry.isDirectory() && !entry.isHidden()) {
                    producerRoots.add(entry);
                    numProducers++;
                }
            }
        }
        
        if (numProducers == 0) {
            producerRoots.add(rootDir);
            numProducers = 1;
        }
        
        // Latch koji broji kada produceri zavrse
        final CountDownLatch producerLatch = new CountDownLatch(numProducers);
        
        // Latch koji signalizira kad su svi consumeri zavrsili
        final CountDownLatch consumerLatch = new CountDownLatch(NUM_CONSUMERS);
        
        StatsPrinter statsPrinter = new StatsPrinter(stats, queue, STATS_INTERVAL_MS);
        Thread statsPrinterThread = new Thread(statsPrinter, "StatsPrinter");
        statsPrinterThread.setDaemon(true);
        statsPrinterThread.start();
        
        List<Thread> producerThreads = new ArrayList<>();
        
        for (File producerRoot : producerRoots) {
            Thread producer = new Thread(() -> {
                try {
                    new FileCrawler(queue, producerRoot, stats, ALLOWED_EXTENSIONS, MAX_FILE_SIZE).run();
                } finally {
                    producerLatch.countDown();
                    System.out.println("[" + Thread.currentThread().getName() + "] Latch decremented. Preostalo: " 
                                       + producerLatch.getCount());
                }
            }, "Producer-" + producerRoot.getName());
            
            producerThreads.add(producer);
            producer.start();
        }
        
        System.out.println("Pokrenuto " + numProducers + " producer niti");
        
        Thread[] consumerThreads = new Thread[NUM_CONSUMERS];
        
        for (int i = 0; i < NUM_CONSUMERS; i++) {
            final int consumerId = i + 1;
            
            consumerThreads[i] = new Thread(() -> {
                int localCount = 0;
                
                try {
                    while (true) {
                        File file = queue.poll(POLL_TIMEOUT_MS, TimeUnit.MILLISECONDS);
                        
                        if (file != null) {
                            FileInfo fileInfo = new FileInfo(file);
                            indexMap.put(fileInfo.getAbsolutePath(), fileInfo);
                            stats.incrementIndexedFiles();
                            localCount++;
                        } else {
                            if (producerLatch.getCount() == 0 && queue.isEmpty()) {
                                break;
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    System.out.println("[Consumer-" + consumerId + "] Zavrsio. Indeksirao: " + localCount);
                    consumerLatch.countDown();
                }
            }, "Consumer-" + consumerId);
            
            consumerThreads[i].start();
        }
        
        System.out.println("Pokrenuto " + NUM_CONSUMERS + " consumer niti\n");
        
        try {
            producerLatch.await();
            System.out.println("\nProducer latch otpusten - svi produceri zavrsili");
            
            consumerLatch.await();
            System.out.println("Consumer latch otpusten - svi consumeri zavrsili\n");
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        statsPrinter.stop();
        statsPrinterThread.interrupt();
        
        long endTime = System.currentTimeMillis();
        System.out.println("\n ZAVRSNI REZULTATI: ");
        System.out.println(stats.getFormattedStats());
        System.out.println("Ukupno u indeksu: " + indexMap.size() + " fajlova");
        System.out.println("Vreme izvrsavanja: " + (endTime - startTime) + " ms");
        
        System.out.println("\nPrimer indeksiranih fajlova (prvih 10):");
        int count = 0;
        for (FileInfo info : indexMap.values()) {
            if (count >= 10) break;
            System.out.println("  " + info);
            count++;
        }
    }
}
