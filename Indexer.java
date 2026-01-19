package dom_2;

import java.io.File;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class Indexer implements Runnable {
    
    private final BlockingQueue<File> queue;
    private final ConcurrentHashMap<String, FileInfo> indexMap;
    private final Statistics stats;
    private final File poisonPill;
    
    private int localIndexCount = 0;
    
    public Indexer(BlockingQueue<File> queue, ConcurrentHashMap<String, FileInfo> indexMap,
                   Statistics stats, File poisonPill) {
        this.queue = queue;
        this.indexMap = indexMap;
        this.stats = stats;
        this.poisonPill = poisonPill;
    }
    
    @Override
    public void run() {
        try {
            while (true) {
                File file = queue.take();
                
                if (file == poisonPill) {
                    queue.put(poisonPill);
                    break;
                }
                
                indexFile(file);
                localIndexCount++;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("[" + Thread.currentThread().getName() + "] Zavrsio. Indeksirao: " + localIndexCount + " fajlova");
    }
    
    private void indexFile(File file) {
        FileInfo fileInfo = new FileInfo(file);
        indexMap.put(fileInfo.getAbsolutePath(), fileInfo);
        stats.incrementIndexedFiles();
    }
    
    public int getLocalIndexCount() {
        return localIndexCount;
    }
}
