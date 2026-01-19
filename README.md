# dom_2 — File indexer

Java project that crawls the filesystem, indexes file metadata and produces statistics.

How it works:

- `FileCrawler` — traverses directories and collects file paths.
- `Indexer` — processes files and creates `FileInfo` records (metadata, size, etc.).
- `FileInfo` — model that holds information about a single file.
- `FileIndexerApp` / `FileIndexerAppBarrier` — example applications that start the indexing (simple and barrier-synchronized variants).
- `Statistics` — aggregates indexing statistics (number of files, total size, distribution by type).
- `StatsPrinter` — prints the collected statistics in a readable format.

Input:

- The program has no interactive input; it runs as a batch application that scans a configured directory (the path can be set in code or passed via configuration).

Sample output:

Root directory: C:\Users\Ilija\eclipse-workspace\dom_2
Number of consumer threads: 10
Queue capacity: 200
Max file size: 5 MB
Allowed extensions: [.java]
Started 3 producer threads
[Producer-bin] Finished crawling: C:\Users\Ilija\eclipse-workspace\dom_2\bin
[Producer-src] Finished crawling: C:\Users\Ilija\eclipse-workspace\dom_2\src
[Producer-.settings] Finished crawling: C:\Users\Ilija\eclipse-workspace\dom_2\.settings
Started 10 consumer threads

[Consumer-10] Finished. Indexed: 0 files
[Consumer-7] Finished. Indexed: 0 files
[Consumer-9] Finished. Indexed: 1 files
[Consumer-2] Finished. Indexed: 1 files
[Consumer-1] Finished. Indexed: 1 files
[Consumer-4] Finished. Indexed: 1 files
[Consumer-3] Finished. Indexed: 2 files
[Consumer-5] Finished. Indexed: 1 files
[Consumer-8] Finished. Indexed: 1 files
[Consumer-6] Finished. Indexed: 2 files

FINAL RESULTS:

STATISTICS
Found in queue: 9
Indexed files: 9
SKIPPED:
  - Disallowed extension: 7
  - Too large: 1
  - Hidden: 0
  - Duplicate: 0
TOTAL skipped: 8

Total in index: 9 files
Execution time: 85 ms

Sample indexed files (first 10):
  FileInfo[path=C:\Users\Ilija\eclipse-workspace\dom_2\src\dom_2\Statistics.java, size=3.1 KB, modified=2026-01-12 13:46:30, ext=.java]
  FileInfo[path=C:\Users\Ilija\eclipse-workspace\dom_2\src\dom_2\FileCrawler.java, size=3.5 KB, modified=2026-01-12 13:16:27, ext=.java]
  FileInfo[path=C:\Users\Ilija\eclipse-workspace\dom_2\src\dom_2\FileIndexerApp.java, size=6.5 KB, modified=2026-01-12 13:46:40, ext=.java]
  FileInfo[path=C:\Users\Ilija\eclipse-workspace\dom_2\src\module-info.java, size=47 B, modified=2026-01-11 13:00:59, ext=.java]
  FileInfo[path=C:\Users\Ilija\eclipse-workspace\dom_2\src\dom_2\StatsPrinter.java, size=1.7 KB, modified=2026-01-12 13:43:47, ext=.java]
  FileInfo[path=C:\Users\Ilija\eclipse-workspace\dom_2\src\dom_2\FileInfo.java, size=1.9 KB, modified=2026-01-12 13:32:10, ext=.java]
  FileInfo[path=C:\Users\Ilija\eclipse-workspace\dom_2\src\dom_2\Indexer.java, size=1.6 KB, modified=2026-01-12 13:34:02, ext=.java]
  FileInfo[path=C:\Users\Ilija\eclipse-workspace\dom_2\src\dom_2\FileIndexerAppBarrier.java, size=6.4 KB, modified=2026-01-12 13:49:59, ext=.java]

Project structure:

- `src/module-info.java` — module declaration
- `src/dom_2/FileCrawler.java` — recursive filesystem traversal
- `src/dom_2/FileIndexerApp.java` — application entry point
- `src/dom_2/FileIndexerAppBarrier.java` — alternative entry with barrier/synchronization
- `src/dom_2/FileInfo.java` — file model
- `src/dom_2/Indexer.java` — indexing logic
- `src/dom_2/Statistics.java` — statistics collection
- `src/dom_2/StatsPrinter.java` — statistics printing



