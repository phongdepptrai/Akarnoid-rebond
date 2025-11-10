package com.arcade.arkanoid.engine.audio;

import javazoom.jl.player.Player;
import java.io.BufferedInputStream;
import java.io.InputStream;

/**
 * Adapter for MP3 audio files using JLayer library.
 * Implements AudioPlayer interface for MP3 format support.
 */
public class Mp3AudioAdapter implements AudioPlayer {
    private final String resourcePath;
    private Thread playerThread;
    private volatile boolean shouldLoop;
    private volatile boolean isPlaying;
    private Player currentPlayer;

    /**
     * Creates an MP3 audio adapter.
     * 
     * @param resourcePath path to the MP3 resource
     */
    public Mp3AudioAdapter(String resourcePath) {
        this.resourcePath = resourcePath;
        this.shouldLoop = false;
        this.isPlaying = false;
    }

    @Override
    public void play() {
        stop();
        shouldLoop = false;
        startPlayback();
    }

    @Override
    public void loop() {
        stop();
        shouldLoop = true;
        startPlayback();
    }

    @Override
    public void stop() {
        isPlaying = false;
        shouldLoop = false;

        // Close current player safely
        synchronized (this) {
            if (currentPlayer != null) {
                try {
                    currentPlayer.close();
                } catch (Exception e) {
                    // Ignore close errors
                }
                currentPlayer = null;
            }
        }

        // Stop thread
        if (playerThread != null && playerThread.isAlive()) {
            playerThread.interrupt();
            try {
                playerThread.join(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public boolean isPlaying() {
        return isPlaying;
    }

    @Override
    public void dispose() {
        stop();
    }

    /**
     * Starts the MP3 playback in a separate thread.
     */
    private void startPlayback() {
        playerThread = new Thread(() -> {
            isPlaying = true;
            do {
                try {
                    InputStream is = getClass().getResourceAsStream(resourcePath);
                    if (is == null) {
                        System.err.println("MP3 resource not found: " + resourcePath);
                        break;
                    }

                    BufferedInputStream bis = new BufferedInputStream(is);

                    // Create player with synchronization
                    synchronized (this) {
                        currentPlayer = new Player(bis);
                    }

                    currentPlayer.play();

                    // Cleanup
                    synchronized (this) {
                        if (currentPlayer != null) {
                            currentPlayer.close();
                            currentPlayer = null;
                        }
                    }

                    bis.close();
                    is.close();

                } catch (Exception e) {
                    if (!Thread.currentThread().isInterrupted()) {
                        System.err.println("Error playing MP3: " + e.getMessage());
                    }
                    break;
                }
            } while (shouldLoop && !Thread.currentThread().isInterrupted());

            isPlaying = false;
        }, "mp3-player-" + resourcePath);

        playerThread.setDaemon(true);
        playerThread.start();
    }
}
