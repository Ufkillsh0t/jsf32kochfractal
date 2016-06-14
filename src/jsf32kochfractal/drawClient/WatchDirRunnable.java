package jsf32kochfractal.drawClient;

import calculate.Edge;
import java.nio.file.*;
import static java.nio.file.StandardWatchEventKinds.*;
import static java.nio.file.LinkOption.*;
import java.nio.file.attribute.*;
import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Example to watch a directory (or tree) for changes to files.
 */
public class WatchDirRunnable implements Runnable {

    private final WatchService watcher;  // the service object who processes events for us
    private final Map<WatchKey, Path> keys; // the map of WatchKey's and belonging Path
    private final boolean recursive;
    private boolean trace = false;
    private JSF32KochFractalDrawClient application;

    private List<Edge> edges;

    /**
     * Creates a WatchService and registers the given directory
     */
    WatchDirRunnable(Path dir, boolean recursive, JSF32KochFractalDrawClient application) throws IOException {
        // create a default WatchService
        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<WatchKey, Path>();  // map of watchkeys and path belonging to it
        this.recursive = recursive;
        this.application = application;

        edges = new ArrayList<>();

        // register all Paths to watch
        if (recursive) {
            System.out.format("Scanning %s ...\n", dir);
            registerAll(dir);
            System.out.println("Done.");
        } else {
            register(dir);
        }

        // enable trace after initial registration
        this.trace = true;
    }

    @SuppressWarnings("unchecked")
    /**
     * utility method to get the context out of the WatchEvent
     */
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>) event;
    }

    /**
     * Register the given directory with the WatchService First, create a
     * WatchKey object which connects all event types to a certain path, and add
     * it to the WatchService. keep an own Map for tracing
     *
     */
    private void register(Path dir) throws IOException {

        WatchKey key = dir.register(this.watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        if (trace) {
            Path prev = keys.get(key);
            if (prev == null) {
                System.out.format("register: %s\n", dir);
            } else if (!dir.equals(prev)) {
                System.out.format("update: %s -> %s\n", prev, dir);
            }
        }
        keys.put(key, dir);
    }

    /**
     * Register the given directory, and all its sub-directories, with the
     * WatchService.
     */
    private void registerAll(final Path start) throws IOException {
        // register directory and sub-directories
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                    throws IOException {
                register(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    @Override
    /**
     * Process all events for keys queued to the watcher.
     */
    public void run() {
        for (;;) {

            // wait for key to be signalled
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                return;
            }

            Path dir = keys.get(key);
            if (dir == null) {
                System.err.println("WatchKey not recognized!!");
                continue;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                // get event kind
                WatchEvent.Kind kind = event.kind();

                // TBD - provide example of how OVERFLOW event is handled
                if (kind == OVERFLOW) {
                    continue;  // something unexpected happened, let's ignore this
                }

                // Context for directory entry event is the file name of entry
                WatchEvent<Path> ev = cast(event);
                Path filename = ev.context();
                Path child = dir.resolve(filename);

                // or you could use: 
                // Path filename = (Path) event.context();
                // this leads to the same result as ev.context() below
                // print out event
                System.out.format("%s: %s\n", event.kind().name(), child);

                System.out.println(filename.toString() + " , " + child);
                
                if (filename.toString().equals("mappedEdges.bin") && (kind == ENTRY_CREATE)) {
                    System.out.println("Found");
                    try {
                        RandomAccessFile file = new RandomAccessFile(new File(child.toUri()), "r");
                        FileChannel fc = file.getChannel();
                        MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_ONLY, 0, file.length());
                        //long length = file.length();
                        while (mbb.hasRemaining()) {
                            double X1 = mbb.getDouble();
                            double Y1 = mbb.getDouble();
                            double X2 = mbb.getDouble();
                            double Y2 = mbb.getDouble();
                            double hue = mbb.getDouble();
                            double saturation = mbb.getDouble();
                            double brightness = mbb.getDouble();

                            edges.add(new Edge(X1, Y1, X2, Y2, hue, saturation, brightness));
                        }
                       
                        application.requestDrawEdges(edges);

                    } catch (FileNotFoundException ex) {
                        Logger.getLogger(WatchDirRunnable.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(WatchDirRunnable.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                // if directory is created, and watching recursively, then
                // register it and its sub-directories
                if (recursive && (kind == ENTRY_CREATE)) {
                    try {
                        if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
                            registerAll(child);
                        }
                    } catch (IOException x) {
                        // ignore to keep sample readable
                    }
                }
            }

            // reset key (because you just handled it, and remove from set if directory no longer accessible
            boolean valid = key.reset();
            if (!valid) {
                keys.remove(key);

                // all directories are inaccessible
                if (keys.isEmpty()) {
                    break;
                }
            }
        }
    }
}
