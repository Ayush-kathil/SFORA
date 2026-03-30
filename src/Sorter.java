import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class Sorter {

    private Map<String, String> wordRules = new HashMap<>();
    private Map<String, String> extensionRules = new HashMap<>();
    
    // keeps track of the file moves so we can undo them later
    private final String logFile = "action_log.txt";

    public Sorter() {
        readCustomRules();
    }

    // --- Organizing logic ---

    public void organizeFiles(File myFolder, String mode) {
        File[] allFiles = myFolder.listFiles();
        if (allFiles == null || allFiles.length == 0) {
            System.out.println("There's nothing here to organize.");
            return;
        }

        System.out.println("\nAlright, starting to move files...");
        int count = 0;
        Map<String, Integer> mathTally = new HashMap<>();

        for (File currentFile : allFiles) {
            // don't try to move directories, the rule file, or our undo log
            if (currentFile.isDirectory() || currentFile.getName().equals(logFile) || currentFile.getName().equals("rules.txt")) {
                continue;
            }

            File targetFolder = figureOutWhereItGoes(currentFile, myFolder, mode);
            if (targetFolder != null) {
                try {
                    // make folder if it doesn't exist
                    targetFolder.getParentFile().mkdirs();

                    File safeDest = fixDuplicateNames(targetFolder);
                    Files.move(currentFile.toPath(), safeDest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    
                    // remember this move in case user clicks undo
                    saveToLog(currentFile.getAbsolutePath(), safeDest.getAbsolutePath());

                    System.out.println("Moved: " + currentFile.getName());
                    count++;

                    String folderName = safeDest.getParentFile().getName();
                    mathTally.put(folderName, mathTally.getOrDefault(folderName, 0) + 1);

                } catch (Exception e) {
                    System.out.println("Could not move: " + currentFile.getName());
                }
            }
        }

        System.out.println("Cleaning up leftover empty folders...");
        int emptyCount = deleteEmptyFolders(myFolder);

        System.out.println("\nAll done!");
        System.out.println("Files moved: " + count);
        System.out.println("Empty folders cleared: " + emptyCount);
        for (String cat : mathTally.keySet()) {
            System.out.println(" -> " + cat + ": " + mathTally.get(cat));
        }
    }

    public void previewChanges(File folder) {
        File[] allFiles = folder.listFiles();
        if (allFiles == null) return;

        System.out.println("\n--- Practice Run (Nothing is actually moving) ---");
        
        int count = 0;
        for (File currentFile : allFiles) {
            if (currentFile.isDirectory() || currentFile.getName().equals(logFile) || currentFile.getName().equals("rules.txt")) {
                continue;
            }

            File targetFolder = figureOutWhereItGoes(currentFile, folder, "hybrid"); 
            if (targetFolder != null) {
                System.out.println("Would move: " + currentFile.getName() + " -> " + targetFolder.getParentFile().getName());
                count++;
            }
        }
        System.out.println("Finished practice run. " + count + " files are ready to move.");
    }

    // --- Extra Features ---

    public void findDuplications(File folder) {
        File[] allFiles = folder.listFiles();
        if (allFiles == null) return;

        System.out.println("\nScanning for copied files...");
        Map<String, String> sizeAndName = new HashMap<>();
        int dups = 0;

        for (File currentFile : allFiles) {
            if (currentFile.isDirectory()) continue;
            
            // simple check: if name and size match exactly, it's probably a duplicate
            String exactMatch = currentFile.getName() + "_" + currentFile.length();
            if (sizeAndName.containsKey(exactMatch)) {
                System.out.println("Found a copy: " + currentFile.getName());
                dups++;
            } else {
                sizeAndName.put(exactMatch, currentFile.getName());
            }
        }
        System.out.println("Finished. Found " + dups + " copied files.");
    }

    public void fixFilenames(File folder) {
        File[] allFiles = folder.listFiles();
        if (allFiles == null) return;

        int count = 0;
        for (File currentFile : allFiles) {
            if (currentFile.isFile() && !currentFile.getName().equals("rules.txt") && !currentFile.getName().equals(logFile)) {
                // strip out weird characters and spaces
                String newName = currentFile.getName().replace(" ", "_").replaceAll("[^a-zA-Z0-9_.-]", "");
                if (!newName.equals(currentFile.getName())) {
                    currentFile.renameTo(new File(folder, newName));
                    System.out.println("Fixed spacing: " + newName);
                    count++;
                }
            }
        }
        System.out.println("Fixed " + count + " messy filenames.");
    }

    public void extractLargeFiles(File folder, long megabytes) {
        File[] allFiles = folder.listFiles();
        if (allFiles == null) return;

        long byteSize = megabytes * 1024 * 1024;
        File giantFolder = new File(folder, "BigFiles");
        int count = 0;

        for (File currentFile : allFiles) {
            if (currentFile.isDirectory() || currentFile.length() < byteSize) continue;

            try {
                if (!giantFolder.exists()) giantFolder.mkdirs();

                File safeDest = fixDuplicateNames(new File(giantFolder, currentFile.getName()));
                Files.move(currentFile.toPath(), safeDest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                
                saveToLog(currentFile.getAbsolutePath(), safeDest.getAbsolutePath());
                System.out.println("Found huge file: " + currentFile.getName());
                count++;
            } catch (Exception e) {}
        }
        System.out.println("Moved " + count + " giant files out of the way.");
    }

    // --- Undo Logic (Logger merged here) ---

    private void saveToLog(String oldPlace, String newPlace) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(logFile, true))) {
            String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            pw.println(oldPlace + " | " + newPlace + " | " + timeStamp);
        } catch (Exception e) {}
    }

    public void undoLast() {
        File log = new File(logFile);
        if (!log.exists()) {
            System.out.println("Nothing to undo.");
            return;
        }

        List<String> list = getSentences(log);
        if (list.isEmpty()) {
            System.out.println("Log is empty. Nothing to undo.");
            return;
        }

        String veryLastAction = list.remove(list.size() - 1);
        if (putFileBack(veryLastAction)) {
            writeSentences(log, list);
            System.out.println("Success! Put the last file back.");
        }
    }

    public void undoAll() {
        File log = new File(logFile);
        if (!log.exists()) return;

        List<String> list = getSentences(log);
        int count = 0;

        // go backward through the list
        for (int i = list.size() - 1; i >= 0; i--) {
            if (putFileBack(list.get(i))) count++;
        }

        System.out.println("Success! We put " + count + " files exactly back where they started.");
        log.delete(); 
    }

    private boolean putFileBack(String row) {
        String[] pieces = row.split(" \\| ");
        if (pieces.length < 2) return false;

        Path original = Paths.get(pieces[0]);
        Path rightNow = Paths.get(pieces[1]);

        try {
            if (Files.exists(rightNow)) {
                if (!Files.exists(original.getParent())) {
                    Files.createDirectories(original.getParent());
                }
                Files.move(rightNow, original, StandardCopyOption.REPLACE_EXISTING);
                return true;
            }
        } catch (Exception e) {
            System.out.println("Oops, could not move " + rightNow.getFileName() + " back.");
        }
        return false;
    }

    private List<String> getSentences(File logic) {
        List<String> text = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(logic))) {
            String line;
            while ((line = br.readLine()) != null) text.add(line);
        } catch (Exception e) {}
        return text;
    }

    private void writeSentences(File logic, List<String> text) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(logic))) {
            for (String s : text) pw.println(s);
        } catch (Exception e) {}
    }

    // --- Rule Engine Logic (Merged here) ---

    private void readCustomRules() {
        File logic = new File("rules.txt");
        if (!logic.exists()) return; 

        try (BufferedReader reader = new BufferedReader(new FileReader(logic))) {
            String row;
            while ((row = reader.readLine()) != null) {
                row = row.trim();
                if (row.isEmpty() || row.startsWith("#")) continue; 

                String[] chunks = row.split(",");
                if (chunks.length == 2) {
                    try {
                        String ruleType = chunks[0].split("=")[0].toUpperCase();
                        String matchWord = chunks[0].split("=")[1].toLowerCase();
                        String destination = chunks[1].split("=")[1];

                        if (ruleType.equals("KEYWORD")) wordRules.put(matchWord, destination);
                        else if (ruleType.equals("EXTENSION")) extensionRules.put(matchWord, destination);
                    } catch (Exception badFormat) {}
                }
            }
        } catch (Exception e) {}
    }

    private File figureOutWhereItGoes(File item, File root, String mode) {
        String name = item.getName().toLowerCase();
        int dotSpot = name.lastIndexOf(".");
        String ext = (dotSpot > 0) ? name.substring(dotSpot + 1) : "unknown";

        String folderTarget = null;

        // check if user wants to override with their own rules
        if (mode.equals("rules") || mode.equals("hybrid")) {
            for (String w : wordRules.keySet()) {
                if (name.contains(w)) folderTarget = wordRules.get(w);
            }
            if (folderTarget == null && extensionRules.containsKey(ext)) {
                folderTarget = extensionRules.get(ext);
            }
        }

        // if there's no custom rule, use default sorting
        if (folderTarget == null && !mode.equals("rules")) {
            if (ext.equals("pdf") || ext.equals("docx") || ext.equals("txt")) folderTarget = "Documents";
            else if (ext.equals("jpg") || ext.equals("png") || ext.equals("jpeg")) folderTarget = "Images";
            else if (ext.equals("mp4") || ext.equals("mp3") || ext.equals("wav")) folderTarget = "Media";
            else if (ext.equals("zip") || ext.equals("rar")) folderTarget = "Archives";
            else folderTarget = "Others";
        }

        if (folderTarget != null) {
            return new File(root, folderTarget + "/" + item.getName());
        }
        return null;
    }

    // --- Helpers ---

    private File fixDuplicateNames(File intendedTarget) {
        File testPath = intendedTarget;
        int count = 1;
        while (testPath.exists()) {
            String name = intendedTarget.getName();
            int dotSpot = name.lastIndexOf(".");
            String basicName = (dotSpot == -1) ? name : name.substring(0, dotSpot);
            String extension = (dotSpot == -1) ? "" : name.substring(dotSpot);
            testPath = new File(intendedTarget.getParentFile(), basicName + "_" + count + extension);
            count++;
        }
        return testPath;
    }

    private int deleteEmptyFolders(File mainLogic) {
        int removed = 0;
        File[] objects = mainLogic.listFiles();
        if (objects != null) {
            for (File obj : objects) {
                if (obj.isDirectory()) removed += deleteEmptyFolders(obj);
            }
        }
        if (mainLogic.listFiles() != null && mainLogic.listFiles().length == 0) {
            if (mainLogic.delete()) return removed + 1;
        }
        return removed;
    }
}
