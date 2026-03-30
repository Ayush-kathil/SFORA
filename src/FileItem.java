import java.io.File;

/**
 * A simple class to hold information about a file we are planning to move.
 * I created this so we can do "Dry Runs" and keep track of sizes.
 */
public class FileItem {

    private File originalFile;
    private File intendedDestination;
    private long fileSizeInBytes;

    public FileItem(File originalFile, File intendedDestination) {
        this.originalFile = originalFile;
        this.intendedDestination = intendedDestination;
        // get the file size so we can warn the user about massive files
        this.fileSizeInBytes = originalFile.length();
    }

    public File getOriginalFile() {
        return originalFile;
    }

    public File getIntendedDestination() {
        return intendedDestination;
    }

    public long getFileSizeInBytes() {
        return fileSizeInBytes;
    }

    // helper method to make the preview look nice
    public boolean isOversized() {
        // flag if bigger than 100MB
        return fileSizeInBytes > (100 * 1024 * 1024);
    }
}
