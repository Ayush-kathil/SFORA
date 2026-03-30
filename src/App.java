import java.io.File;
import java.util.Scanner;
import javax.swing.SwingUtilities;

public class App {
    
    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        
        System.out.println("----- File Organizer Project -----");
        System.out.println("Type 1 to run in Terminal (CLI)");
        System.out.println("Type 2 to run visually (GUI)");
        System.out.print("> ");

        if (!scan.hasNextLine()) return;
        String choice = scan.nextLine().trim();

        if (choice.equals("1")) {
            System.out.println("\n--- Terminal Mode ---\n");
            runCLI(scan);
        } else if (choice.equals("2")) {
            System.out.println("\n--- Loading GUI Window ---\n");
            SwingUtilities.invokeLater(() -> {
                GUI window = new GUI();
                window.setLocationRelativeTo(null); 
                window.setVisible(true);
            });
        } else {
            System.out.println("Wrong input. Starting in terminal anyway.");
            runCLI(scan);
        }
    }
    
    // handles the terminal logic natively
    public static void runCLI(Scanner scan) {
        Sorter mySorter = new Sorter();
        File folder = null;

        while (folder == null) {
            System.out.println("Enter the folder path you want to organize:");
            System.out.print("> ");
            if (!scan.hasNextLine()) return;
            String input = scan.nextLine().trim();
            File dir = new File(input);
            if (dir.exists() && dir.isDirectory()) {
                folder = dir;
            } else {
                System.out.println("That folder doesn't exist. Try another path.");
            }
        }

        boolean keepRunning = true;
        while (keepRunning) {
            System.out.println("\nWhat would you like to do?");
            System.out.println("1. Organize my files");
            System.out.println("2. Preview (don't move anything yet)");
            System.out.println("3. Undo my last action");
            System.out.println("4. Undo everything (reset)");
            System.out.println("5. Find any duplicates");
            System.out.println("6. Clean up weird filenames");
            System.out.println("7. Move really large files");
            System.out.println("8. Quit program");
            System.out.print("> ");

            if (!scan.hasNextLine()) return;
            String choice = scan.nextLine().trim();

            switch (choice) {
                case "1":
                    System.out.println("Organize mode? (type 'rules' or 'hybrid')");
                    System.out.print("> ");
                    String mode = scan.nextLine().trim().toLowerCase();
                    mySorter.organizeFiles(folder, mode);
                    break;
                case "2":
                    mySorter.previewChanges(folder);
                    break;
                case "3":
                    mySorter.undoLast();
                    break;
                case "4":
                    mySorter.undoAll();
                    break;
                case "5":
                    mySorter.findDuplications(folder);
                    break;
                case "6":
                    mySorter.fixFilenames(folder);
                    break;
                case "7":
                    System.out.print("Enter giant file size limit in MB (e.g., 50): ");
                    try {
                        long size = Long.parseLong(scan.nextLine().trim());
                        mySorter.extractLargeFiles(folder, size);
                    } catch (Exception e) {
                        System.out.println("That's not a number.");
                    }
                    break;
                case "8":
                    System.out.println("Bye!");
                    keepRunning = false;
                    break;
                default:
                    System.out.println("Just type a number from 1 to 8.");
            }
        }
    }
}
