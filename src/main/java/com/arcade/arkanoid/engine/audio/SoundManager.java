package com.arcade.arkanoid.engine.audio;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class SoundManager {
    private final Map<String, Clip> clips = new HashMap<>();

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

    public void play(String id) {
        Clip clip = clips.get(id);
        if (clip == null) {
            return;
        }
        if (clip.isRunning()) {
            clip.stop();
        }
        clip.setFramePosition(0);
        clip.start();
    }

    public void loop(String id) {
        Clip clip = clips.get(id);
        if (clip == null) {
            return;
        }
        clip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    public void stop(String id) {
        Clip clip = clips.get(id);
        if (clip != null && clip.isRunning()) {
            clip.stop();
        }
    }

    public void dispose() {
        clips.values().forEach(Clip::close);
        clips.clear();
    }
}
