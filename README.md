<div align="center">

# 🗂️ Smart File Organizer (SFORA)
**A high-performance, 100% pure Java automated file management system.**

[![Java Version](https://img.shields.io/badge/Java-8%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://java.com/)
[![CLI Interface](https://img.shields.io/badge/Interface-CLI-000000?style=for-the-badge&logo=windows-terminal)](https://github.com/)
[![Dependencies](https://img.shields.io/badge/Dependencies-0-success?style=for-the-badge)](#)

*Eliminate digital clutter in milliseconds using native Java automation.*

</div>

---

## ⚡ Overview
SFORA (Smart File Organizer with Rule-Based Automation) is a lightweight yet extremely powerful desktop utility designed to securely organize chaotic file directories (such as the `Downloads` folder). 

It dynamically scans, categorizes, and safely relocates files into structured taxonomies while actively recording every transaction, allowing users to safely automate complex filesystem management without sacrificing security or control. 

> [!NOTE]
> **Minimalist Engineering:** SFORA operates absolutely free of external dependencies or bloated third-party libraries. Built entirely on the native `java.io` and `java.nio` standard libraries, it guarantees absolute cross-platform execution (Windows, macOS, Linux).

---

## 🚀 Key Features

### 🎯 1. Intelligent Taxonomy Routing
Automatically funnels heterogeneous files into universally understood directories based on their strict data extensions.
* `.pdf`, `.docx`, `.txt` ➡️ `/Documents`
* `.png`, `.jpg`, `.svg` ➡️ `/Images`
* `.mp4`, `.mp3`, `.wav` ➡️ `/Media`

### ⚙️ 2. Dynamic Rule Configuration (`rules.txt`)
Takes priority over basic file extensions. Empower the system to isolate files based on explicit keyword mapping. For example, automatically route any file containing the string `"assignment"` directly into a `/University` directory, bypassing normal rules.

### 🛡️ 3. "Dry Run" Environment Preview
Visualizes the exact data relocation payload *before* touching a single byte on the physical drive. Instantly calculates potential move counts and uniquely flags massive, storage-hungry files (`> 100MB`) in the preview console.

### 🕰️ 4. Persistent Memento Undo (`action_log.txt`)
Never fear losing data. Every atomic movement executes a parallel disk-write to a hardened log file mapping the `Source ➡️ Destination`. The program natively reverse-engineers this exact log allowing you to execute a highly robust **Full Rollback** even days after the initial transaction.

### 🧹 5. Advanced System Cleanup
- **Duplicate Execution:** Isolates redundant file clones matching precise byte-lengths ($O(N)$ string mapping) into a dedicated `/Duplicates` silo.
- **Space Savers:** Allows users to input a precise MB integer threshold (e.g. `> 1GB`) and automatically isolates massive system ISOs or archives in seconds.
- **Topological Scrubbing:** Automatically purges empty directory nodes post-execution, preventing digital "ghost-folder" fragmenting.

---

## 💻 Execution Protocol

Deploying the framework requires zero environmental setup outside of a base JDK installation.

**1. Establish Binary Path**
```bash
mkdir bin
javac -d bin src/*.java
```

**2. Boot Application Interface**
```bash
java -cp bin Main
```

---

## ⌨️ Interface Preview
SFORA uses a locked-context, continuous 7-option execution loop designed for effortless terminal interaction.

```text
---------------------------------
     SMART FILE ORGANIZER
---------------------------------
Enter folder path:
> C:\Downloads

Then show options:
1. Organize files (recommended)
2. Preview changes (no files will be moved)
3. Undo last change
4. Undo all changes
5. Find duplicate files
6. Clean file names
7. Extract large files (Space Saver)
8. Exit
> 1

How do you want to organize it?
- Organize by file type
- Organize by rules
- Hybrid (recommended)
> hybrid
```
