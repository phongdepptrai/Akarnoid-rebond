package com.arcade.arkanoid.engine.assets;

import com.arcade.arkanoid.engine.util.IOThreadPool;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * Asset manager with I/O Thread Pool for async resource loading.
 */
public class AssetManager {
    private final Map<String, BufferedImage> images = new HashMap<>();
    private final IOThreadPool ioThreadPool;

    public AssetManager() {
        this.ioThreadPool = IOThreadPool.getInstance();
    }

    /**
     * Load image asynchronously on I/O Thread.
     */
    public Future<?> loadImageAsync(String id, String resourcePath) {
        return ioThreadPool.submit(() -> {
            loadImage(id, resourcePath);
        });
    }

    /**
     * Load image synchronously (blocking).
     */
    public void loadImage(String id, String resourcePath) {
        if (images.containsKey(id)) {
            return;
        }
        try {
            URL url = getClass().getResource(resourcePath);
            if (url == null) {
                System.err.println("Image resource not found: " + resourcePath);
                return;
            }
            BufferedImage image = ImageIO.read(url);
            synchronized (images) {
                images.put(id, image);
            }
        } catch (IOException e) {
            System.err.println("Unable to load image " + resourcePath + ": " + e.getMessage());
        }
    }

    public BufferedImage getImage(String id) {
        synchronized (images) {
            return images.get(id);
        }
    }

    public void clear() {
        synchronized (images) {
            images.clear();
        }
    }
}
