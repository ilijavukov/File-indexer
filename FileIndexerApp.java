package dom_2;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;


public class FileIndexerApp {
        
    
    // Kapacitet BlockingQueue reda
    private static final int QUEUE_CAPACITY = 100;
    
    // Broj consumer niti 
    private static final int NUM_CONSUMERS = Runtime.getRuntime().availableProcessors();
    
    // Maksimalna velicina fajla za indeksiranje (10 MB)
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    
    // Dozvoljene ekstenzije za indeksiranje
    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(
        ".java"
    ));
    
    // Interval za periodicnu statistiku (ms)
    private static final long STATS_INTERVAL_MS = 1000;
    
    private static final File POISON_PILL = new File("");
    
    public static void main(String[] args) {
        // Odredjivanje putanje direktorijuma
        String path;
        if (args.length == 0) {         
            path = "C:\\Users\\Ilija\\eclipse-workspace\\dom_2";
        } else if (args.length == 1) {
            path = args[0];
        } else {
            // Spajanje argumenata ako putanja ima razmake
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < args.length; i++) {
                if (i > 0) sb.append(" ");
                sb.append(args[i]);
            }
            path = sb.toString();
        }
        
        File rootDir = new File(path);
        
        // Provera da li direktorijum postoji i da li je direktorijum
        if (!rootDir.exists()) {
            System.out.println("Greska: Direktorijum ne postoji: " + args[0]);
            System.exit(1);
        }
        
        if (!rootDir.isDirectory()) {
            System.out.println("Greska: Putanja nije direktorijum: " + args[0]);
            System.exit(1);
        }
        
        System.out.println("Koreni direktorijuma: " + rootDir.getAbsolutePath());
        System.out.println("Broj consumer niti: " + NUM_CONSUMERS);
        System.out.println("Kapacitet reda: " + QUEUE_CAPACITY);
        System.out.println("Max velicina fajla: " + (MAX_FILE_SIZE / 1024 / 1024) + " MB");
        System.out.println("Dozvoljene ekstenzije: " + ALLOWED_EXTENSIONS);
        
        // Pokreni indeksiranje
        startIndexing(rootDir);
    }
    
    public static void startIndexing(File rootDir) {
        long startTime = System.currentTimeMillis();
        
        // Kreiranje deljenih struktura podataka
        BlockingQueue<File> queue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);
        ConcurrentHashMap<String, FileInfo> indexMap = new ConcurrentHashMap<>();
        Statistics stats = new Statistics();
        
        // Liste za cuvanje niti
        List<Thread> producerThreads = new ArrayList<>();
        Thread[] consumerThreads = new Thread[NUM_CONSUMERS];
        
        StatsPrinter statsPrinter = new StatsPrinter(stats, queue, STATS_INTERVAL_MS);
        Thread statsPrinterThread = new Thread(statsPrinter, "StatsPrinter");
        statsPrinterThread.setDaemon(true); // Daemon nit - zavrsava kad zavrse ostale
        statsPrinterThread.start();
        
        // Kreiram jednog producera za svaki poddirektorijum korenog direktorijuma
        File[] rootEntries = rootDir.listFiles();
        
        if (rootEntries != null && rootEntries.length > 0) {
            for (File entry : rootEntries) {
                if (entry.isDirectory() && !entry.isHidden()) {
                    Thread producer = new Thread(
                        new FileCrawler(queue, entry, stats, ALLOWED_EXTENSIONS, MAX_FILE_SIZE),
                        "Producer-" + entry.getName()
                    );
                    producerThreads.add(producer);
                    producer.start();
                } else if (entry.isFile()) {

                }
            }
        }
        
        // Ako nema poddirektorijuma, kreiram jednog producera za ceo root
        if (producerThreads.isEmpty()) {
            Thread producer = new Thread(
                new FileCrawler(queue, rootDir, stats, ALLOWED_EXTENSIONS, MAX_FILE_SIZE),
                "Producer-Root"
            );
            producerThreads.add(producer);
            producer.start();
        }
        
        System.out.println("Pokrenuto " + producerThreads.size() + " producer niti");
        
        for (int i = 0; i < NUM_CONSUMERS; i++) {
            consumerThreads[i] = new Thread(
                new Indexer(queue, indexMap, stats, POISON_PILL),
                "Consumer-" + (i + 1)
            );
            consumerThreads[i].start();
        }
        
        System.out.println("Pokrenuto " + NUM_CONSUMERS + " consumer niti\n");
        
        for (Thread producer : producerThreads) {
            try {
                producer.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        try {
            queue.put(POISON_PILL);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        for (Thread consumer : consumerThreads) {
            try {
                consumer.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }        
        
        statsPrinter.stop();
        statsPrinterThread.interrupt();
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        System.out.println("ZAVRSNI REZULTATI: ");
        System.out.println(stats.getFormattedStats());
        System.out.println("Ukupno u indeksu: " + indexMap.size() + " fajlova");
        System.out.println("Vreme izvrsavanja: " + duration + " ms");
        
        System.out.println("\nPrimer indeksiranih fajlova (prvih 10):");
        int count = 0;
        for (FileInfo info : indexMap.values()) {
            if (count >= 10) break;
            System.out.println("  " + info);
            count++;
        }
        
        if (indexMap.size() > 10) {
            System.out.println("  ... i jos " + (indexMap.size() - 10) + " fajlova");
        }
    }
}
