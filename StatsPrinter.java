package dom_2;

import java.io.File;
import java.util.concurrent.BlockingQueue;

public class StatsPrinter implements Runnable {
    
    private final Statistics stats;
    private final BlockingQueue<File> queue;
    private final long intervalMs;
    private volatile boolean running = true;

    public StatsPrinter(Statistics stats, BlockingQueue<File> queue, long intervalMs) {
        this.stats = stats;
        this.queue = queue;
        this.intervalMs = intervalMs;
    }
    
    @Override
    public void run() {   
        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(intervalMs);
                printStats();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
    }
    
    private void printStats() {
        System.out.println("\n--- Periodicna statistika ---");
        System.out.println("Trenutna velicina reda: " + queue.size());
        System.out.println("Pronadjeno fajlova: " + stats.getFoundFiles());
        System.out.println("Indeksirano fajlova: " + stats.getIndexedFiles());
        System.out.println("Preskoceno - ekstenzija: " + stats.getSkippedByExtension());
        System.out.println("Preskoceno - velicina: " + stats.getSkippedBySize());
        System.out.println("Preskoceno - hidden: " + stats.getSkippedByHidden());
        System.out.println("Preskoceno - duplikat: " + stats.getSkippedByDuplicate());
        System.out.println("-----------------------------\n");
    }
    
    public void stop() {
        running = false;
    }
}
