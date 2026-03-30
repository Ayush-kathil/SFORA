import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;

/**
 * The main logic file. This actually touches the hard drive and moves stuff.
 * It also handles the "Preview" mode so we don't accidentally ruin everything.
 */
public class FileOrganizer {

    private RuleEngine rules;
    private FileLogger logger;

    // A small cache to keep track of duplicates while we run
    private Map<String, Path> duplicateCache = new HashMap<>();

    public FileOrganizer() {
        this.rules = new RuleEngine();
        this.logger = new FileLogger();
    }

    // A quick hack to find out what kind of file it is from the extension
    private String getCategory(String filename) {
        String name = filename.toLowerCase();
        if (name.endsWith(".pdf") || name.endsWith(".docx") || name.endsWith(".txt") || name.endsWith(".xlsx")) return "Documents";
        if (name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".jpeg") || name.endsWith(".svg")) return "Images";
        if (name.endsWith(".mp4") || name.endsWith(".mp3") || name.endsWith(".wav") || name.endsWith(".mkv")) return "Media";
        if (name.endsWith(".zip") || name.endsWith(".rar") || name.endsWith(".tar") || name.endsWith(".7z")) return "Archives";
        return "Others"; // stuff we don't recognize
    }

    // Fixes weird spaces and characters in filenames without actually moving them out of their folder
    public void cleanFileNames(File directory) {
        File[] items = directory.listFiles();
        if (items == null) return;

        int cleaned = 0;
        for (File f : items) {
            if (f.isFile() && !f.getName().equals("rules.txt") && !f.getName().equals("action_log.txt")) {
                String original = f.getName();
                String safe = original.replace(" ", "_").replaceAll("[^a-zA-Z0-9_.-]", "");
                
                if (!original.equals(safe)) {
                    File renamed = new File(f.getParent(), safe);
                    f.renameTo(renamed);
                    cleaned++;
                    System.out.println("Cleaned: " + original + " -> " + safe);
                }
            }
        }
        System.out.println("Total files cleaned: " + cleaned);
    }

    // Scans a folder to print duplicates using hashing (so we don't get fooled by weird filenames)
    public void findDuplicatesPreview(File directory) {
        System.out.println("\n--- Scanning for exact Duplicates... ---");
        File[] items = directory.listFiles();
        if (items == null) return;

        int dups = 0;
        duplicateCache.clear(); // clear any old runs

        for (File f : items) {
            if (f.isFile() && !f.getName().equals("action_log.txt")) {
                String hash = getFileHash(f);
                if (hash != null) {
                    if (duplicateCache.containsKey(hash)) {
                        System.out.println("Duplicate found! '" + f.getName() + "' is identical to '" + duplicateCache.get(hash).toFile().getName() + "'");
                        dups++;
                    } else {
                        duplicateCache.put(hash, f.toPath());
                    }
                }
            }
        }
        System.out.println("Finished scanning. Found " + dups + " identical duplicate files.");
    }

    // Simulates what *would* happen if we ran it for real.
    public void dryRunPreview(File directory, String mode) {
        System.out.println("\n--- DRY RUN (Preview) ---");
        System.out.println("No actual files will be moved during this preview.\n");

        List<FileItem> simulatedMoves = planMoves(directory, mode);

        int hugeFiles = 0;
        long totalData = 0;

        for (FileItem item : simulatedMoves) {
            System.out.println("WILL MOVE: " + item.getOriginalFile().getName() + " -> " + item.getIntendedDestination().getPath());
            totalData += item.getFileSizeInBytes();
            if (item.isOversized()) hugeFiles++;
        }

        System.out.println("\n--- Predicted Summary ---");
        System.out.println("Total Files to Route: " + simulatedMoves.size());
        System.out.println("Giant Files (>100MB): " + hugeFiles + " (might want to check these!)");
        System.out.println("Total Size Organized: " + (totalData / (1024 * 1024)) + " MB");
        System.out.println("------------------------\n");
    }

    // The actual massive execution method
    public void executeOrganization(File directory, String mode, boolean safeMode, boolean deleteEmptyFolders, Scanner scanner) {
        List<FileItem> plannedMoves = planMoves(directory, mode);

        if (plannedMoves.isEmpty()) {
            System.out.println("No files needed organizing here.");
            return;
        }

        // Safe mode asks the user for confirmation BEFORE destroying their structure
        if (safeMode) {
            System.out.println("\n[SAFE MODE] Ready to move " + plannedMoves.size() + " files.");
            System.out.print("Are you absolutely sure you want to proceed? (y/n): ");
            String ans = scanner.nextLine().trim();
            if (!ans.equalsIgnoreCase("y")) {
                System.out.println("Organization aborted by user.");
                return;
            }
        }

        System.out.println("\nStarting organization...");
        
        int successCount = 0;
        Map<String, Integer> categorySummary = new HashMap<>();

        for (FileItem item : plannedMoves) {
            try {
                // create target folder if it doesn't exist
                if (!item.getIntendedDestination().getParentFile().exists()) {
                    item.getIntendedDestination().getParentFile().mkdirs();
                }

                // If duplicate file name exists in target folder, generate a _1 suffix
                File finalDest = getUniqueDestination(item.getIntendedDestination());

                Files.move(item.getOriginalFile().toPath(), finalDest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                
                // Keep track that we moved this in the persistent log file!
                logger.logMove(item.getOriginalFile().getAbsolutePath(), finalDest.getAbsolutePath());
                
                successCount++;

                // Build a quick summary of what we are doing
                String category = finalDest.getParentFile().getName();
                categorySummary.put(category, categorySummary.getOrDefault(category, 0) + 1);

            } catch (Exception e) {
                System.out.println("Failed to move file: " + item.getOriginalFile().getName());
            }
        }

        // Post-process feature: delete empty roots left behind
        if (deleteEmptyFolders) {
            cleanEmptyDirectories(directory);
        }

        System.out.println("\n--- FINAL REPORT ---");
        System.out.println("Successfully organized " + successCount + " files.");
        System.out.println("Breakdown:");
        for (String cat : categorySummary.keySet()) {
            System.out.println("- " + cat + " : " + categorySummary.get(cat));
        }
        System.out.println("--------------------");
    }

    // Core logic that decides where files should go based on the chosen Strategy mode
    private List<FileItem> planMoves(File directory, String mode) {
        List<FileItem> plans = new ArrayList<>();
        duplicateCache.clear(); // refresh duplicate session cache

        File[] files = directory.listFiles();
        if (files == null) return plans;

        for (File file : files) {
            // Ignore folders and our own project's log/rule files
            if (file.isDirectory() || file.getName().equals("rules.txt") || file.getName().equals("action_log.txt")) {
                continue;
            }

            String filename = file.getName();
            int dotIndex = filename.lastIndexOf(".");
            String extension = (dotIndex > 0) ? filename.substring(dotIndex + 1) : "";

            File targetFolder = null;

            // 1. DUPLICATE CHECK ALWAYS FIRST
            String hash = getFileHash(file);
            if (hash != null && duplicateCache.containsKey(hash)) {
                targetFolder = new File(directory, "Duplicates");
            } else {
                if (hash != null) duplicateCache.put(hash, file.toPath());

                // 2. ROUTING STRATEGY
                if (mode.equalsIgnoreCase("rules")) {
                    String ruleFolder = rules.getTargetFolderForRule(filename, extension);
                    if (ruleFolder != null) {
                        targetFolder = new File(directory, ruleFolder);
                    }
                } else if (mode.equalsIgnoreCase("type")) {
                    targetFolder = new File(directory, getCategory(filename));
                } else {
                    // Hybrid mode: rules override types
                    String ruleFolder = rules.getTargetFolderForRule(filename, extension);
                    if (ruleFolder != null) {
                        targetFolder = new File(directory, ruleFolder);
                    } else {
                        targetFolder = new File(directory, getCategory(filename));
                    }
                }
            }

            // If we found a destination and it isn't literally where it already is
            if (targetFolder != null) {
                File intendedDest = new File(targetFolder, filename);
                plans.add(new FileItem(file, intendedDest));
            }
        }

        return plans;
    }

    // makes sure we don't accidentally overwrite a completely different file that happens to have the same name
    private File getUniqueDestination(File targetFile) {
        File finalFile = targetFile;
        int counter = 1;

        while (finalFile.exists()) {
            String name = targetFile.getName();
            int dot = name.lastIndexOf(".");
            String base = (dot == -1) ? name : name.substring(0, dot);
            String ext = (dot == -1) ? "" : name.substring(dot);
            
            finalFile = new File(targetFile.getParent(), base + "_" + counter + ext);
            counter++;
        }
        return finalFile;
    }

    // recursive cleanup
    private void cleanEmptyDirectories(File directory) {
        File[] children = directory.listFiles();
        if (children != null) {
            for (File child : children) {
                if (child.isDirectory()) cleanEmptyDirectories(child);
            }
        }
        // only delete if absolutely empty and it isn't the main root directory itself
        if (directory.listFiles() != null && directory.listFiles().length == 0) {
            directory.delete();
            System.out.println("Swept away empty leftover folder: " + directory.getName());
        }
    }

    // Hashing helper
    private String getFileHash(File f) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            try (InputStream is = Files.newInputStream(f.toPath())) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = is.read(buffer)) > 0) {
                    md.update(buffer, 0, read);
                }
            }
            StringBuilder hex = new StringBuilder();
            for (byte b : md.digest()) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (Exception e) {
            return null;
        }
    }
}
