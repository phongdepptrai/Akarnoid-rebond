package com.arcade.arkanoid.engine.util;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.InputStream;

public class FontLoader {

    private static boolean loaded = false;

    public static void loadAll() {
        if (loaded) return;

        String[] fonts = {
            "/fonts/Orbitron-Regular.ttf",
            "/fonts/Orbitron-Bold.ttf",
            "/fonts/Orbitron-ExtraBold.ttf",
            "/fonts/Orbitron-Medium.ttf",
            "/fonts/Orbitron-Black.ttf",
            "/fonts/Orbitron-SemiBold.ttf",
            "/fonts/generation.ttf",
            "/fonts/optimus.otf",
            "/fonts/emulogic.ttf"
        };

        for (String path : fonts) {
            try (InputStream is = FontLoader.class.getResourceAsStream(path)) {
                if (is == null) {
                    System.err.println("Error loading font: " + path);
                    continue;
                }
                Font font = Font.createFont(Font.TRUETYPE_FONT, is);
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                ge.registerFont(font);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        loaded = true;
    }

    public static Font get(String name, int style, int size) {
        return new Font(name, style, size);
    }
}