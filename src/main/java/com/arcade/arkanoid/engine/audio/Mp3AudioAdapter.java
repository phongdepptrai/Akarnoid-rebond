package com.arcade.arkanoid.engine.audio;

import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.AudioDeviceBase;
import javazoom.jl.player.advanced.AdvancedPlayer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
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
    private AdvancedPlayer currentPlayer;
    private final Object deviceLock = new Object();
    private volatile float volume = 1.0f;
    private AdjustableAudioDevice activeDevice;

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
        synchronized (deviceLock) {
            activeDevice = null;
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

                    AdjustableAudioDevice device = new AdjustableAudioDevice();
                    device.setVolume(volume);
                    synchronized (deviceLock) {
                        activeDevice = device;
                    }

                    // Create player with synchronization
                    synchronized (this) {
                        currentPlayer = new AdvancedPlayer(bis, device);
                    }

                    currentPlayer.play();

                    // Cleanup
                    synchronized (this) {
                        if (currentPlayer != null) {
                            currentPlayer.close();
                            currentPlayer = null;
                        }
                    }

                    synchronized (deviceLock) {
                        if (device == activeDevice) {
                            activeDevice = null;
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

    @Override
    public void setVolume(float volume) {
        float clamped = clampVolume(volume);
        this.volume = clamped;
        synchronized (deviceLock) {
            if (activeDevice != null) {
                activeDevice.setVolume(clamped);
            }
        }
    }

    @Override
    public float getVolume() {
        return volume;
    }

    private static float clampVolume(float value) {
        if (Float.isNaN(value)) {
            return 0f;
        }
        return Math.max(0f, Math.min(1f, value));
    }

    private static final class AdjustableAudioDevice extends AudioDeviceBase {
        private SourceDataLine source;
        private AudioFormat format;
        private byte[] byteBuffer = new byte[4096];
        private volatile float volume = 1.0f;

        @Override
        protected void openImpl() {
            // Lazy open when data arrives.
        }

        @Override
        protected void closeImpl() {
            if (source != null) {
                source.stop();
                source.close();
                source = null;
            }
            format = null;
        }

        @Override
        protected void writeImpl(short[] samples, int offs, int len) throws JavaLayerException {
            ensureSource();
            byte[] data = toByteArray(samples, offs, len);
            source.write(data, 0, len * 2);
        }

        @Override
        protected void flushImpl() {
            if (source != null) {
                source.drain();
            }
        }

        @Override
        public int getPosition() {
            if (source != null) {
                return (int) (source.getMicrosecondPosition() / 1000);
            }
            return 0;
        }

        void setVolume(float volume) {
            this.volume = Mp3AudioAdapter.clampVolume(volume);
        }

        private void ensureSource() throws JavaLayerException {
            if (source != null) {
                return;
            }
            AudioFormat fmt = getOrCreateFormat();
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, fmt);
            try {
                source = (SourceDataLine) AudioSystem.getLine(info);
                source.open(fmt);
                source.start();
            } catch (LineUnavailableException ex) {
                throw new JavaLayerException("Cannot obtain audio line", ex);
            }
        }

        private AudioFormat getOrCreateFormat() {
            if (format == null) {
                Decoder decoder = getDecoder();
                if (decoder != null) {
                    format = new AudioFormat(decoder.getOutputFrequency(), 16, decoder.getOutputChannels(), true, false);
                } else {
                    format = new AudioFormat(44100, 16, 2, true, false);
                }
            }
            return format;
        }

        private byte[] toByteArray(short[] samples, int offs, int len) {
            byte[] buffer = ensureBuffer(len * 2);
            int index = 0;
            float currentVolume = volume;
            for (int i = 0; i < len; i++) {
                short sample = samples[offs + i];
                if (currentVolume < 0.999f) {
                    sample = (short) (sample * currentVolume);
                }
                buffer[index++] = (byte) sample;
                buffer[index++] = (byte) (sample >>> 8);
            }
            return buffer;
        }

        private byte[] ensureBuffer(int needed) {
            if (byteBuffer.length < needed) {
                byteBuffer = new byte[needed + 1024];
            }
            return byteBuffer;
        }
    }
}
