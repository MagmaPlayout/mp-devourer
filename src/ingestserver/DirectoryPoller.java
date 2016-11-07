package ingestserver;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Map;

/**
 * Runs in a Thread and watches a given directory, notifying changes and
 * processing when necessary
 *
 * @author cyberpunx
 */
public class DirectoryPoller extends Thread {

    String DirectoryAbsolutePath;
    Boolean isPolling;
    FileProcessor fp;

    /**
     * A directory to be analyzed
     *
     * @param absolutePath absolute path to directory
     */
    public DirectoryPoller(String absolutePath) {
        this.DirectoryAbsolutePath = absolutePath;
        this.isPolling = false;
        fp = new FileProcessor();
    }

    /**
     * Starts the Thread, analyzing the directory and processing when something
     * changes
     */
    @Override
    public void run() {
        System.out.println("Polling started at " + this.DirectoryAbsolutePath);
        this.isPolling = true;

        try (WatchService service = FileSystems.getDefault().newWatchService()) {
            Map<WatchKey, Path> keyMap = new HashMap<>();
            Path path = Paths.get(DirectoryAbsolutePath);
            keyMap.put(path.register(service,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE,
                    StandardWatchEventKinds.ENTRY_MODIFY),
                    path
            );
            WatchKey watchKey;

            do {
                watchKey = service.take();
                Path eventDir = keyMap.get(watchKey);

                for (WatchEvent<?> event : watchKey.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    Path eventPath;
                    eventPath = (Path) event.context();
                    if (fp.isCorrectFileType(eventPath)) {
                        // Process the modified files
                        processFiles(eventDir, kind, eventPath);
                    }
                }

            } while (watchKey.reset() && this.isPolling);
        } catch (Exception e) {

        }
    }

    private void processFiles(Path eventDir, WatchEvent.Kind<?> kind, Path eventPath) {
        System.out.println(eventDir + ": " + kind + ": " + eventPath);
    }

    /**
     * Stops the Thread.
     */
    public void stopPolling() {
        this.isPolling = false;
        System.out.println("Polling stopped");
        Thread.currentThread().interrupt();
    }

}
