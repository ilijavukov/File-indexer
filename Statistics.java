package dom_2;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Statistics {
    
    // Brojac pronadjenih fajlova
    private final AtomicInteger foundFiles = new AtomicInteger(0);
    
    // Brojac uspesno indeksiranih fajlova
    private final AtomicInteger indexedFiles = new AtomicInteger(0);
    
    // Brojaci za preskocene fajlove po razlozima
    private final AtomicInteger skippedByExtension = new AtomicInteger(0);
    private final AtomicInteger skippedBySize = new AtomicInteger(0);
    private final AtomicInteger skippedByHidden = new AtomicInteger(0);
    private final AtomicInteger skippedByDuplicate = new AtomicInteger(0);
    
    // Mapa za pracenje vec posecenih putanja
    private final ConcurrentHashMap<String, Boolean> visitedPaths = new ConcurrentHashMap<>();
    
    public boolean markVisitedIfNew(String canonicalPath) {
        return visitedPaths.putIfAbsent(canonicalPath, Boolean.TRUE) == null;
    }
    
    // Metode za inkrementiranje brojaca
    public void incrementFoundFiles() {
        foundFiles.incrementAndGet();
    }
    
    public void incrementIndexedFiles() {
        indexedFiles.incrementAndGet();
    }
    
    public void incrementSkippedByExtension() {
        skippedByExtension.incrementAndGet();
    }
    
    public void incrementSkippedBySize() {
        skippedBySize.incrementAndGet();
    }
    
    public void incrementSkippedByHidden() {
        skippedByHidden.incrementAndGet();
    }
    
    public void incrementSkippedByDuplicate() {
        skippedByDuplicate.incrementAndGet();
    }
    
    // Metode za citanje vrednosti
    public int getFoundFiles() {
        return foundFiles.get();
    }
    
    public int getIndexedFiles() {
        return indexedFiles.get();
    }
    
    public int getSkippedByExtension() {
        return skippedByExtension.get();
    }
    
    public int getSkippedBySize() {
        return skippedBySize.get();
    }
    
    public int getSkippedByHidden() {
        return skippedByHidden.get();
    }
    
    public int getSkippedByDuplicate() {
        return skippedByDuplicate.get();
    }
    
    public int getTotalSkipped() {
        return skippedByExtension.get() + skippedBySize.get() + 
               skippedByHidden.get() + skippedByDuplicate.get();
    }
    
    public String getFormattedStats() {
        StringBuilder sb = new StringBuilder();
        sb.append("\nSTATISTIKA \n");
        sb.append(String.format("Pronadjenih fajlova (u redu): %d\n", foundFiles.get()));
        sb.append(String.format("Indeksiranih fajlova: %d\n", indexedFiles.get()));
        sb.append("PRESKOCENO: \n");
        sb.append(String.format("  - Nedozvoljena ekstenzija: %d\n", skippedByExtension.get()));
        sb.append(String.format("  - Prevelik fajl: %d\n", skippedBySize.get()));
        sb.append(String.format("  - Sakriven (hidden): %d\n", skippedByHidden.get()));
        sb.append(String.format("  - Duplikat: %d\n", skippedByDuplicate.get()));
        sb.append(String.format("UKUPNO preskoceno: %d\n", getTotalSkipped()));
        return sb.toString();
    }
}
