# File Organizer Project (SFORA)

This is a Java-based desktop application designed to automatically sort and clean up messy directories (like a typical `Downloads` folder). It organizes files into appropriate sub-folders based on file extensions or custom keywords. I built this purely using standard Java I/O (`java.io`) and a basic Swing GUI (`javax.swing`), ensuring it requires absolutely zero external libraries.

## 📌 Project Overview
The application functions as a hybrid tool. When executed, it allows the user to run standard Terminal interactions (CLI) or a graphical window (GUI). The core mechanism iterates through a specified directory tree and physically moves files using `Files.move()` into target folders according to simple conditions.

## ✨ Key Features
1. **Hybrid Interface**: Supports both a command-line prompt and a Native Java Swing window. 
2. **File Sorting**: Automatically reads file extensions (like `.pdf` or `.png`) and creates categorical folders (`/Documents`, `/Images`) to store them.
3. **Custom Sorting Rules**: The application reads a local `rules.txt` file at runtime. You can specify custom mapping (e.g., if a file name contains "assignment", force it to a `/University` folder).
4. **Duplicate Finder**: Finds basic exact duplicate files by checking if they share the identical name and byte size in memory.
5. **Action Logging (Undo)**: Every time a file is fundamentally moved, its source and destination are recorded in `action_log.txt`. The program can read this text file backward to restore changed files completely safely.
6. **Big File Search**: Scans and isolates massive video or zip files exceeding a specified Megabyte limit into a separate folder.

## 📂 File Structure
The project is intentionally kept simple and compressed into three fundamental Java files:
*   `App.java`: Contains the `main` method. It prompts the user for CLI/GUI selection and houses the `Scanner` terminal loop.
*   `GUI.java`: Contains the layout for the Application Window using `BorderLayout` and custom black/white button themes.
*   `Sorter.java`: The backend utility script that executes the logic (directory loops, duplicate hashing, and text-file writing).

## 💻 How to Compile & Run

To evaluate the application, open your terminal (Command Prompt, PowerShell, or bash) in the root directory where the `src/` folder is located.

**Step 1. Compile the code:**
```bash
mkdir bin
javac -d bin src/*.java
```

**Step 2. Run the program:**
```bash
java -cp bin App
```

*Note: Once executed, simply follow the on-screen prompts to navigate between the Terminal and Graphical window.*
