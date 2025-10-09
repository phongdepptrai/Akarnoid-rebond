package com.arcade.arkanoid.engine.core;

public final class GameConfig {
    private final int width;
    private final int height;
    private final String title;
    private final int targetFps;

    public GameConfig(int width, int height, String title, int targetFps) {
        this.width = width;
        this.height = height;
        this.title = title;
        this.targetFps = targetFps;
    }

    public static GameConfig defaultConfig() {
        return new GameConfig(1280, 720, "Arkanoid Reborn", 60);
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public String title() {
        return title;
    }

    public int targetFps() {
        return targetFps;
    }
}
