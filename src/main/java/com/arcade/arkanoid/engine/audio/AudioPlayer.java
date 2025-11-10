package com.arcade.arkanoid.engine.audio;

/**
 * Interface for audio playback using Adapter Pattern.
 * Defines common operations for different audio formats.
 */
public interface AudioPlayer {
    void play();

    void loop();

    void stop();

    boolean isPlaying();

    void dispose();
}
