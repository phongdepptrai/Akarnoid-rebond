package com.arcade.arkanoid.engine.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * I/O Thread Pool for async file operations.
 * Architecture: Separate thread for Save/Load game and Load resources.
 */
public class IOThreadPool {
    private static IOThreadPool instance;
    private final ExecutorService ioThread;

    private IOThreadPool() {
        // Single I/O Thread for file operations
        this.ioThread = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "io-thread");
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * Singleton instance.
     */
    public static synchronized IOThreadPool getInstance() {
        if (instance == null) {
            instance = new IOThreadPool();
        }
        return instance;
    }

    /**
     * Submit async I/O task.
     */
    public Future<?> submit(Runnable task) {
        return ioThread.submit(task);
    }

    /**
     * Submit async I/O task with callback.
     */
    public <T> Future<T> submit(java.util.concurrent.Callable<T> task) {
        return ioThread.submit(task);
    }

    /**
     * Shutdown I/O thread.
     */
    public void shutdown() {
        ioThread.shutdown();
    }
}
