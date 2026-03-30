import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Reads my custom rules.txt file so I can tell the program exactly where
 * specific files should go based on their names or extensions.
 */
public class RuleEngine {

    // storing rules as a map: pattern -> destination folder
    // checking both keyword (in filename) and straight extensions
    private Map<String, String> keywordRules = new HashMap<>();
    private Map<String, String> extensionRules = new HashMap<>();

    public RuleEngine() {
        loadRules("rules.txt");
    }

    private void loadRules(String fileName) {
        File ruleFile = new File(fileName);
        if (!ruleFile.exists()) {
            System.out.println("No rules.txt found. I'll just use the default folders.");
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(ruleFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                // skip empty lines or comments
                if (line.trim().isEmpty() || line.startsWith("#")) {
                    continue;
                }

                // expecting stuff like: KEYWORD=assignment,FOLDER=University
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    String condition = parts[0].trim();
                    String folder = parts[1].trim();

                    String[] condSplit = condition.split("=");
                    String[] foldSplit = folder.split("=");

                    if (condSplit.length == 2 && foldSplit.length == 2) {
                        String type = condSplit[0].toUpperCase();
                        String pattern = condSplit[1].toLowerCase();
                        String dest = foldSplit[1];

                        if (type.equals("KEYWORD")) {
                            keywordRules.put(pattern, dest);
                        } else if (type.equals("EXTENSION")) {
                            extensionRules.put(pattern, dest);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Oops, couldn't read the rules file: " + e.getMessage());
        }
    }

    // Checking if a file matches any of our custom rules
    public String getTargetFolderForRule(String filename, String extension) {
        filename = filename.toLowerCase();
        
        // try keywords first
        for (Map.Entry<String, String> entry : keywordRules.entrySet()) {
            if (filename.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        // try extension overrides next
        if (extension != null && extensionRules.containsKey(extension.toLowerCase())) {
            return extensionRules.get(extension.toLowerCase());
        }

        return null; // no rule matched
    }
}
