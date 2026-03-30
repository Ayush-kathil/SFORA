# SFORA: Smart File Organizer

Honestly, my Downloads folder was an absolute disaster. I couldn't find a single PDF or image because everything was just dumped into one massive list. I got tired of manually dragging files around every week, so I decided to learn file I/O and build **SFORA** over a couple of late nights.

It’s a command-line tool that just *reads your messy folder* and automatically moves all the files into neat folders like `Documents`, `Images`, and `Archives` based on their type. It even lets you set custom rules in a text file.

## Why did I build this?
1. **Automation**: Why do manually what Java can do in literally 30 milliseconds?
2. **Duplicate Finding**: I kept downloading the same assignment files (`project(1).pdf`, `project(2).pdf`). SFORA actually reads the byte hash of the file and catches identical duplicates!
3. **Fear of Mistakes**: Moving hundreds of files at once is scary. I added a **Dry Run (Preview)** mode so I can see what it *will* do before it actually touches my hard drive.
4. **Undo Saves Lives**: I built a `FileLogger` module. Every single time a file gets moved, it logs the exact path to an `action_log.txt` file. If I mess up, I can just select "Undo All Changes" and it literally puts the entire directory back exactly how it was!

## Cool Features I Added
- **Interactive UI**: No confusing flags or commands. Just run the file and follow the 1-7 menu.
- **Safe Mode**: It will prompt you `Are you sure? (y/n)` before it ruins your folder structure if you want it to.
- **Large File Detector**: Warns you if there are giant files (like 4GB video files) taking up space during a preview scan!
- **Empty Folder Cleaner**: SFORA deletes left-behind empty folders so things stay pristine.

## How to use it
It’s completely written in core Java, so there’s no weird setups required.
Just compile the 6 files like this:
```
mkdir bin
javac -d bin src/*.java
```

And then run the entry point:
```
java -cp bin Main
```

### Sample Output (Dry Run)
```
What would you like to do?
1 - Start File Organizer
2 - Undo Last Operation
3 - Undo All Changes (Full Rollback)
4 - Preview Changes (Dry Run)
5 - Clean File Names Automatically
6 - Find Exact Duplicates
7 - Exit Program
> Pick an option (1-7): 4

--- DRY RUN (Preview) ---
No actual files will be moved during this preview.

WILL MOVE: math_assignment.pdf -> C:\Downloads\University\math_assignment.pdf
WILL MOVE: funny_cat.jpg -> C:\Downloads\Images\funny_cat.jpg

--- Predicted Summary ---
Total Files to Route: 2
Giant Files (>100MB): 0 (might want to check these!)
Total Size Organized: 34 MB
------------------------
```

Hope this saves you as much time as it saved me!
