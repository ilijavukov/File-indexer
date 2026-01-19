package dom_2;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileInfo {
    
    private final String absolutePath;
    private final long sizeInBytes;
    private final long lastModified;
    private final String extension;
    
    public FileInfo(File file) {
        this.absolutePath = file.getAbsolutePath();
        this.sizeInBytes = file.length();
        this.lastModified = file.lastModified();
        this.extension = extractExtension(file.getName());
    }
    
    private String extractExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex).toLowerCase();
        }
        return "";
    }
    
    public String getAbsolutePath() {
        return absolutePath;
    }
    
    public long getSizeInBytes() {
        return sizeInBytes;
    }
    
    public long getLastModified() {
        return lastModified;
    }
    
    public String getExtension() {
        return extension;
    }
    
    public String getFormattedLastModified() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date(lastModified));
    }
    
    public String getFormattedSize() {
        if (sizeInBytes < 1024) {
            return sizeInBytes + " B";
        } else if (sizeInBytes < 1024 * 1024) {
            return String.format("%.2f KB", sizeInBytes / 1024.0);
        } else {
            return String.format("%.2f MB", sizeInBytes / (1024.0 * 1024.0));
        }
    }
    
    @Override
    public String toString() {
        return String.format("FileInfo[path=%s, size=%s, modified=%s, ext=%s]",
                absolutePath, getFormattedSize(), getFormattedLastModified(), extension);
    }
}
