package com.arcade.arkanoid.engine.audio;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Sound manager with dedicated Audio Thread Pool.
 * Architecture: Async audio playback on separate threads.
 */
public class SoundManager {
    private final Map<String, Clip> clips = new HashMap<>();
    private final ExecutorService audioThreadPool;

    public SoundManager() {
        // Audio Thread Pool - for async sound effects and background music
        this.audioThreadPool = Executors.newFixedThreadPool(4, r -> {
            Thread t = new Thread(r, "audio-thread");
            t.setDaemon(true);
            return t;
        });
    }

    public void load(String id, String resourcePath) {
        if (clips.containsKey(id)) {
            return;
        }
        try {
            URL url = getClass().getResource(resourcePath);
            if (url == null) {
                System.err.println("Sound resource not found: " + resourcePath);
                return;
            }
            try (AudioInputStream stream = AudioSystem.getAudioInputStream(url)) {
                Clip clip = AudioSystem.getClip();
                clip.open(stream);
                clips.put(id, clip);
            }
        } catch (Exception e) {
            System.err.println("Unable to load sound " + resourcePath + ": " + e.getMessage());
        }
    }

    /**
     * Play sound effect on Audio Thread Pool (async).
     */
    public void play(String id) {
        audioThreadPool.submit(() -> {
            Clip clip = clips.get(id);
            if (clip == null)
                return;

            synchronized (clip) {
                if (clip.isRunning()) {
                    clip.stop();
                }
                clip.setFramePosition(0);
                clip.start();
            }
        });
    }

    /**
     * Loop background music on Audio Thread Pool (async).
     */
    public void loop(String id) {
        audioThreadPool.submit(() -> {
            Clip clip = clips.get(id);
            if (clip == null)
                return;

            synchronized (clip) {
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            }
        });
    }

    /**
     * Stop sound on Audio Thread Pool (async).
     */
    public void stop(String id) {
        audioThreadPool.submit(() -> {
            Clip clip = clips.get(id);
            if (clip != null && clip.isRunning()) {
                synchronized (clip) {
                    clip.stop();
                }
            }
        });
    }

    public void dispose() {
        audioThreadPool.shutdown();
        clips.values().forEach(Clip::close);
        clips.clear();
    }
}
