## SFORA (Smart File Organizer with Rule-Based Automation)

### Abstract
Digital clutter is a persistent problem for computer users, especially in root directories like the `Downloads` folder, where various types of documents, installers, and media accumulate rapidly. Manually analyzing and sorting these files is time-consuming and inefficient. This project is a core Java desktop application designed to evaluate and automate this entire process natively. Without relying on heavy external frameworks, it leverages standard Java I/O capabilities coupled with a cleanly partitioned Java AWT/Swing graphical window to allow users to easily categorize, rename, and extract files.

### System Architecture
The application is structured linearly to maximize readability and adhere strictly to 2nd-year Java curriculum standards. It consists of three localized Java files:

1. **`App.java`**: The program entry point. It hosts a hybrid selector to bootstrap the User Interface onto the Swing Event Dispatch Thread safely, while simultaneously maintaining a standard `Scanner` Terminal logic loop for CLI testing.
2. **`GUI.java`**: Handles basic user interactions utilizing fundamental Swing mechanics (`JPanel`, `JTextField`, `JFileChooser`). It captures the user's directory path and intercepts the raw Terminal output streams (`System.setOut`), redirecting internal logic strings natively into a visual `JTextArea`.
3. **`Sorter.java`**: The workhorse of the application. It natively accesses system file trees to traverse directories, execute basic Name/Size checking for duplicates, and safely relocate byte data. It also directly parses configuration thresholds outlined in `rules.txt` and manages a continuous text buffer in `action_log.txt` to enable a safe "Undo" system.

### Core Implementation
*   **Intuitive Desktop Interface**: Employs standard `javax.swing` array configurations featuring standard event action listeners mapped accurately to logical sorting loops.
*   **Sequential Sorting**: Objects are automatically categorized and sorted based on their functional extensions (`.mp4` logically maps to `Media/`, `.pdf` to `Documents/`).
*   **File State Safety**: Ensures that files can be reliably manipulated backward to their original point of origin across entirely different application sessions by recursively checking a local text log.
*   **Large Target Filtering**: Automates iterations across local directory streams and surgically isolates components exceeding a user-defined mathematical threshold boundary into a separate folder.

### Project Justification
To ensure maximum compatability across multiple grading platforms without configuration mismatching, this project was authored entirely in the native Java Standard Library. The complete lack of external tools or complex design patterns guarantees the program compiles identically via raw commands regardless of the evaluator's system configuration.
