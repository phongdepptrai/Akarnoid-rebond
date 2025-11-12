package com.arcade.arkanoid.engine.audio;

public class StageMusicManager {
    private static StageMusicManager instance;
    private final SoundManager soundManager;
    private boolean isPlaying;
    private boolean isPaused;
    private String currentStageMusic;
    private float masterVolume = 1.0f;

    private StageMusicManager() {
        this.soundManager = new SoundManager();
        this.isPlaying = false;
        this.isPaused = false;
    }

    public static synchronized StageMusicManager getInstance() {
        if (instance == null) {
            instance = new StageMusicManager();
        }
        return instance;
    }

    public void playStageMusic(String musicId, String resourcePath) {
        if (isPlaying && !isPaused && musicId.equals(currentStageMusic)) {
            return;
        }

        if (isPaused && musicId.equals(currentStageMusic)) {
            resume();
            return;
        }

        // Stop current music if different
        if (currentStageMusic != null && !musicId.equals(currentStageMusic)) {
            stop();
        }

        // Load and play new music
        soundManager.setGlobalVolume(masterVolume);
        soundManager.load(musicId, resourcePath);
        soundManager.loop(musicId);
        currentStageMusic = musicId;
        isPlaying = true;
        isPaused = false;
    }

    public void pause() {
        if (isPlaying && !isPaused && currentStageMusic != null) {
            soundManager.stop(currentStageMusic);
            isPaused = true;
        }
    }

    public void resume() {
        if (isPlaying && isPaused && currentStageMusic != null) {
            soundManager.setGlobalVolume(masterVolume);
            soundManager.loop(currentStageMusic);
            isPaused = false;
        }
    }

    public void stop() {
        if (currentStageMusic != null) {
            soundManager.stop(currentStageMusic);
            currentStageMusic = null;
            isPlaying = false;
            isPaused = false;
        }
    }

    public boolean isPlaying() {
        return isPlaying && !isPaused;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public String getCurrentStageMusic() {
        return currentStageMusic;
    }

    public void setVolume(float volume) {
        masterVolume = Math.max(0f, Math.min(1f, volume));
        soundManager.setGlobalVolume(masterVolume);
    }

    public float getVolume() {
        return masterVolume;
    }
}
