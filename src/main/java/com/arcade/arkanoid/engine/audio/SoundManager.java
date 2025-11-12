package com.arcade.arkanoid.engine.audio;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Sound manager with dedicated Audio Thread Pool.
 * Architecture: Uses Adapter Pattern to support multiple audio formats (WAV,
 * MP3).
 * Uses Factory Pattern to create appropriate audio players.
 */
public class SoundManager {
    private final Map<String, AudioPlayer> players = new HashMap<>();
    private final ExecutorService audioThreadPool;
    private volatile float globalVolume = 1.0f;

    public SoundManager() {
        // Audio Thread Pool - for async sound effects and background music
        this.audioThreadPool = Executors.newFixedThreadPool(4, r -> {
            Thread t = new Thread(r, "audio-thread");
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * Loads an audio file using the appropriate adapter.
     * 
     * @param id           unique identifier for the audio
     * @param resourcePath path to the audio resource
     */
    public void load(String id, String resourcePath) {
        if (players.containsKey(id)) {
            return;
        }

        try {
            // Use Factory Pattern to create appropriate adapter
            AudioPlayer player = AudioPlayerFactory.createPlayer(resourcePath);
            player.setVolume(globalVolume);
            players.put(id, player);
        } catch (Exception e) {
            System.err.println("Unable to load sound " + resourcePath + ": " + e.getMessage());
        }
    }

    /**
     * Play sound effect on Audio Thread Pool (async).
     */
    public void play(String id) {
        audioThreadPool.submit(() -> {
            AudioPlayer player = players.get(id);
            if (player != null) {
                player.play();
            }
        });
    }

    /**
     * Loop background music on Audio Thread Pool (async).
     */
    public void loop(String id) {
        audioThreadPool.submit(() -> {
            AudioPlayer player = players.get(id);
            if (player != null) {
                player.loop();
            }
        });
    }

    /**
     * Stop sound on Audio Thread Pool (async).
     */
    public void stop(String id) {
        audioThreadPool.submit(() -> {
            AudioPlayer player = players.get(id);
            if (player != null) {
                player.stop();
            }
        });
    }

    /**
     * Stop all currently playing sounds.
     */
    public void stopAll() {
        audioThreadPool.submit(() -> {
            players.values().forEach(AudioPlayer::stop);
        });
    }

    /**
     * Disposes all audio resources and shuts down the thread pool.
     */
    public void dispose() {
        audioThreadPool.shutdown();
        players.values().forEach(AudioPlayer::dispose);
        players.clear();
    }

    public void setGlobalVolume(float volume) {
        float clamped = clampVolume(volume);
        globalVolume = clamped;
        players.values().forEach(player -> player.setVolume(clamped));
    }

    public float getGlobalVolume() {
        return globalVolume;
    }

    private static float clampVolume(float volume) {
        if (Float.isNaN(volume)) {
            return 0f;
        }
        return Math.max(0f, Math.min(1f, volume));
    }
}
