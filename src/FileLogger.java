import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Saves and loads the history of moved files using a physical action_log.txt file.
 * This is super important so if I close the program, I can still "Undo" everything tomorrow!
 */
public class FileLogger {

    private static final String LOG_FILE = "action_log.txt";

    // logs a move to the text file
    public void logMove(String originalPath, String newPath) {
        try (FileWriter fw = new FileWriter(LOG_FILE, true);
             PrintWriter pw = new PrintWriter(fw)) {
            // using a simple pipe delimiter 
            pw.println(originalPath + "|" + newPath + "|" + System.currentTimeMillis());
        } catch (IOException e) {
            System.out.println("Failed to write to action log: " + e.getMessage());
        }
    }

    // reads the entire log file and moves everything back to where it was
    // from the bottom up!
    public void undoAllOperations() {
        File log = new File(LOG_FILE);
        if (!log.exists()) {
            System.out.println("There is no action_log.txt to undo from.");
            return;
        }

        List<String[]> operations = new ArrayList<>();

        // read all lines
        try (BufferedReader br = new BufferedReader(new FileReader(log))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 2) {
                    operations.add(parts);
                }
            }
        } catch (IOException e) {
            System.out.println("Couldn't read log file.");
            return;
        }

        if (operations.isEmpty()) {
            System.out.println("No operations found in log.");
            return;
        }

        System.out.println("Okay, starting a full reversal of " + operations.size() + " files...");
        int successCount = 0;

        // we need to go backwards (last in, first out) to avoid conflicts
        for (int i = operations.size() - 1; i >= 0; i--) {
            String[] op = operations.get(i);
            String oldPath = op[0]; // where it originally was
            String currentPath = op[1]; // where we moved it to

            try {
                Path source = Paths.get(currentPath);
                Path destination = Paths.get(oldPath);

                if (Files.exists(source)) {
                    // recreate original folders just in case they were deleted
                    if (!Files.exists(destination.getParent())) {
                        Files.createDirectories(destination.getParent());
                    }
                    Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);
                    successCount++;
                }
            } catch (Exception e) {
                System.out.println("Undo failed for: " + currentPath);
            }
        }

        System.out.println("Successfully undid " + successCount + " files!");
        
        // wipe the log after a full undo so it's fresh
        log.delete();
    }

    // Just undos the very last line in the text file
    public void undoLastOperation() {
        File log = new File(LOG_FILE);
        if (!log.exists()) {
            System.out.println("No history to undo!");
            return;
        }

        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(log))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) { return; }

        if (lines.isEmpty()) {
            System.out.println("Nothing left to undo.");
            return;
        }

        // grab the last action
        String lastLine = lines.get(lines.size() - 1);
        String[] parts = lastLine.split("\\|");
        
        try {
            Path source = Paths.get(parts[1]);
            Path destination = Paths.get(parts[0]);
            
            if (!Files.exists(destination.getParent())) {
                Files.createDirectories(destination.getParent());
            }

            Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Reverted: " + source.getFileName() + " to its original place.");

            // now rewrite the log without the last line
            lines.remove(lines.size() - 1);
            try (PrintWriter pw = new PrintWriter(new FileWriter(log))) {
                for (String l : lines) {
                    pw.println(l);
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to revert the last file.");
        }
    }
}
