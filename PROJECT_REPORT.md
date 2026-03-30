# Project Report / Reflection: SFORA

## Introduction
I developed the Smart File Organizer with Rule Automation (SFORA) to tackle a very real, annoying problem: digital clutter. Over semesters, standard directories like `Downloads` fill up with thousands of mixed file types, creating massive overhead when trying to search for specific documents. SFORA uses standard Java File I/O to parse, categorize, and deduplicate files entirely automatically.

## Design Choices
Initially, I thought about building a huge array of interfaces and manager classes, but I realized that was total overkill. Instead, my approach was highly practical and straightforward. All the code sits in `src/` inside 6 clear, purpose-built Java files:
- `ConsoleUI`: Handles the interactive prompts.
- `FileOrganizer`: Has the core logic to route files.
- `FileLogger`: A fun feature I added to persist moves to an `action_log.txt` file, allowing cross-session "Undo" functionality.

## Technical Highlights
One of the hardest challenges was dealing with duplicates. Filenames aren't enough because people often download `Resume.pdf` and `Resume (1).pdf`. So, I implemented `java.security.MessageDigest` to calculate the SHA-256 byte signature of every file. It compares the raw bytes, so it catches duplicates flawlessly.

Another piece I'm proud of is the `Dry Run` (Preview) mode. Using a simple `FileItem` POJO, I was able to simulate the entire sorting algorithm, calculate data payloads, and print a summary table to the console so the user knows what *will* happen before the hard drive actually gets modified. It even warns you about files bigger than 100MB!

## Conclusion
Building this taught me a massive amount about Stream handling, File Systems, and how crucial persistent state tracking (`action_log.txt`) is when building tools that manipulate sensitive data. SFORA is lightweight but highly impactful.
