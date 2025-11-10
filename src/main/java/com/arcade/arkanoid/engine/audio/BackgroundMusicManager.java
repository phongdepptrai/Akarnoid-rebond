package com.arcade.arkanoid.engine.audio;

/**
 * Singleton manager for background music.
 * Ensures only one instance of background music plays across all scenes.
 * Music continues playing when switching between menu scenes.
 */
public class BackgroundMusicManager {
    private static BackgroundMusicManager instance;
    private final SoundManager soundManager;
    private String currentTheme;

    private BackgroundMusicManager() {
        this.soundManager = new SoundManager();
    }

    /**
     * Gets the singleton instance.
     * 
     * @return the BackgroundMusicManager instance
     */
    public static synchronized BackgroundMusicManager getInstance() {
        if (instance == null) {
            instance = new BackgroundMusicManager();
        }
        return instance;
    }

    /**
     * Plays a theme song. If the same theme is already playing, does nothing.
     * 
     * @param themeId      unique identifier for the theme
     * @param resourcePath path to the MP3 resource
     */
    public void playTheme(String themeId, String resourcePath) {
        if (themeId.equals(currentTheme)) {
            // Same theme already playing, do nothing
            return;
        }

        // Stop current theme if different
        if (currentTheme != null) {
            soundManager.stop(currentTheme);
        }

        // Load and play new theme
        soundManager.load(themeId, resourcePath);
        soundManager.loop(themeId);
        currentTheme = themeId;
    }

    /**
     * Stops the currently playing theme.
     */
    public void stopTheme() {
        if (currentTheme != null) {
            soundManager.stop(currentTheme);
            currentTheme = null;
        }
    }

    /**
     * Gets the currently playing theme ID.
     * 
     * @return the current theme ID, or null if no theme is playing
     */
    public String getCurrentTheme() {
        return currentTheme;
    }
}
