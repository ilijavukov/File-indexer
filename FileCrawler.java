package dom_2;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

public class FileCrawler implements Runnable {
    
    private final BlockingQueue<File> fileQueue;
    private final File root;
    private final Statistics stats;
    private final Set<String> allowedExtensions;
    private final long maxFileSize;
  
    public FileCrawler(BlockingQueue<File> fileQueue, File root, Statistics stats,
                       Set<String> allowedExtensions, long maxFileSize) {
        this.fileQueue = fileQueue;
        this.root = root;
        this.stats = stats;
        this.allowedExtensions = allowedExtensions;
        this.maxFileSize = maxFileSize;
    }
    
    @Override
    public void run() {
        try {
            crawl(root);
            System.out.println("[" + Thread.currentThread().getName() + "] Zavrsio obilazak: " + root.getPath());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("[" + Thread.currentThread().getName() + "] Prekinut!");
        }
    }
    
    private void crawl(File directory) throws InterruptedException {
        // Provera da li je direktorijum sakriven
        if (directory.isHidden()) {
            return; 
        }
        
        File[] entries = directory.listFiles();
        if (entries == null) {
            return; // Nema pristupa ili prazan direktorijum
        }
        
        for (File entry : entries) {
            // Provera prekida
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }
            
            if (entry.isDirectory()) {
                // Rekurzivni poziv za poddirektorijum
                crawl(entry);
            } else {
                // Obrada fajla
                processFile(entry);
            }
        }
    }
    
    private void processFile(File file) throws InterruptedException {
        // 1. Provera da li je fajl sakriven
        if (file.isHidden()) {
            stats.incrementSkippedByHidden();
            return;
        }
        
        // 2. Provera ekstenzije
        String extension = getExtension(file.getName());
        if (!allowedExtensions.contains(extension)) {
            stats.incrementSkippedByExtension();
            return;
        }
        
        // 3. Provera velicine
        if (file.length() > maxFileSize) {
            stats.incrementSkippedBySize();
            return;
        }
        
        // 4. Koristimo kanonicku putanju
        String canonicalPath;
        try {
            canonicalPath = file.getCanonicalPath();
        } catch (IOException e) {
            // Ako ne mozemo dobiti kanonicku putanju, koristimo apsolutnu
            canonicalPath = file.getAbsolutePath();
        }
        
        // Provera da li je vec posecen
        if (!stats.markVisitedIfNew(canonicalPath)) {
            stats.incrementSkippedByDuplicate();
            return;
        }
        
        // Fajl prolazi sve provere
        fileQueue.put(file);
        stats.incrementFoundFiles();
    }
    
    private String getExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex).toLowerCase();
        }
        return "";
    }
}
