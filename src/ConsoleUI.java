import java.io.File;
import java.util.Scanner;

/**
 * The Console User Interface.
 * Built to be highly human-readable and interactive without technical jargon.
 */
public class ConsoleUI {

    private Scanner scanner = new Scanner(System.in);
    private FileOrganizer organizer = new FileOrganizer();
    private FileLogger logger = new FileLogger();

    public void start() {
        System.out.println("===============================");
        System.out.println("        Welcome to SFORA       ");
        System.out.println(" (Smart File Organizer Automation)");
        System.out.println("===============================");

        boolean keepRunning = true;
        while (keepRunning) {
            System.out.println("\nWhat would you like to do?");
            System.out.println("1 - Start File Organizer");
            System.out.println("2 - Undo Last Operation");
            System.out.println("3 - Undo All Changes (Full Rollback)");
            System.out.println("4 - Preview Changes (Dry Run)");
            System.out.println("5 - Clean File Names Automatically");
            System.out.println("6 - Find Exact Duplicates");
            System.out.println("7 - Exit Program");
            System.out.print("> Pick an option (1-7): ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    handleStartOrganizer();
                    break;
                case "2":
                    logger.undoLastOperation();
                    break;
                case "3":
                    System.out.println("Are you sure? This will put every file back to its exact original place! (y/n)");
                    if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
                        logger.undoAllOperations();
                    }
                    break;
                case "4":
                    handleDryRun();
                    break;
                case "5":
                    System.out.print("Enter full path of folder to clean filenames: ");
                    String cleanTarget = scanner.nextLine().trim();
                    organizer.cleanFileNames(new File(cleanTarget));
                    break;
                case "6":
                    System.out.print("Enter full path of folder to scan for deeply hidden duplicates: ");
                    String dupTarget = scanner.nextLine().trim();
                    organizer.findDuplicatesPreview(new File(dupTarget));
                    break;
                case "7":
                    System.out.println("Exiting SFORA. Thanks for keeping things tidy!");
                    keepRunning = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please select 1 through 7.");
            }
        }
    }

    private void handleStartOrganizer() {
        System.out.print("\nEnter the absolute path of the messy folder: ");
        String path = scanner.nextLine().trim();
        File folder = new File(path);

        if (!folder.exists() || !folder.isDirectory()) {
            System.out.println("Uh oh, that directory doesn't seem to exist.");
            return;
        }

        System.out.println("\nHow do you want to organize this?");
        System.out.println("A - by File Type (puts all PDFs in 'Documents', JPGs in 'Images')");
        System.out.println("B - by Rules (strictly follows your custom rules.txt file)");
        System.out.println("C - Hybrid (Recommended: follows rules, then falls back to File Types)");
        System.out.print("> Select A, B, or C: ");
        
        String strategy = scanner.nextLine().trim().toUpperCase();
        String mode;
        if (strategy.equals("A")) mode = "type";
        else if (strategy.equals("B")) mode = "rules";
        else mode = "hybrid";

        System.out.print("Enable Safe Mode? (It will ask you right before moving anything) (y/n): ");
        boolean safeMode = scanner.nextLine().trim().equalsIgnoreCase("y");

        System.out.print("Clean up empty useless folders after moving? (y/n): ");
        boolean deleteEmptyFolders = scanner.nextLine().trim().equalsIgnoreCase("y");
        
        // Auto-enabling undo statement
        System.out.println("\n[Note] 'Undo all changes for this session' will automatically be enabled via the persistent action_log.txt.");

        // Hand it off to the actual engine
        organizer.executeOrganization(folder, mode, safeMode, deleteEmptyFolders, scanner);
    }

    private void handleDryRun() {
        System.out.print("\nEnter the absolute path of the folder to preview: ");
        String path = scanner.nextLine().trim();
        File folder = new File(path);

        if (!folder.exists() || !folder.isDirectory()) {
            System.out.println("Uh oh, that directory does not exist.");
            return;
        }
        
        // We'll just run a Hybrid preview by default as it's the most powerful
        organizer.dryRunPreview(folder, "hybrid");
    }
}
