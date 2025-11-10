package com.arcade.arkanoid.engine.audio;

/**
 * Factory for creating MP3 AudioPlayer.
 * Only supports MP3 format.
 */
public class AudioPlayerFactory {

    /**
     * Creates an AudioPlayer for the given resource path.
     * 
     * @param resourcePath path to the MP3 audio resource
     * @return Mp3AudioAdapter instance
     * @throws Exception if unable to create player or format is not MP3
     */
    public static AudioPlayer createPlayer(String resourcePath) throws Exception {
        if (resourcePath.toLowerCase().endsWith(".mp3")) {
            return new Mp3AudioAdapter(resourcePath);
        } else {
            throw new IllegalArgumentException("Only MP3 format is supported: " + resourcePath);
        }
    }
}
