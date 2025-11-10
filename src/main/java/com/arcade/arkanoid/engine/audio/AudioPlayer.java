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

    /**
     * Adjusts playback volume. Range expected between 0.0 (mute) and 1.0 (full).
     * Default implementation is a no-op for players without volume control.
     */
    default void setVolume(float volume) {
    }

    /**
     * Returns the current playback volume. Default is full volume.
     */
    default float getVolume() {
        return 1.0f;
    }
}
