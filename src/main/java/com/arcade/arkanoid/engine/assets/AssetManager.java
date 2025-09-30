package com.arcade.arkanoid.engine.assets;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class AssetManager {
    private final Map<String, BufferedImage> images = new HashMap<>();

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
            images.put(id, image);
        } catch (IOException e) {
            System.err.println("Unable to load image " + resourcePath + ": " + e.getMessage());
        }
    }

    public BufferedImage getImage(String id) {
        return images.get(id);
    }

    public void clear() {
        images.clear();
    }
}
