package com.arcade.arkanoid.engine.input;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;

public class InputManager implements KeyListener {
    private static final int KEY_RANGE = 512;
    private final boolean[] pressed = new boolean[KEY_RANGE];
    private final int[] pendingJustPressedCount = new int[KEY_RANGE]; // Count presses
    private final boolean[] frameJustPressed = new boolean[KEY_RANGE];
    private final long[] pressedTime = new long[KEY_RANGE]; // Track when key was pressed
    private static final long REPEAT_DELAY = 500_000_000L; // 120ms delay before repeat
    private static final long REPEAT_INTERVAL = 40_000_000L; // 40ms between repeats (~25 repeats/sec)

    public synchronized boolean isKeyPressed(int keyCode) {
        return keyCode >= 0 && keyCode < KEY_RANGE && pressed[keyCode];
    }

    public synchronized boolean isKeyJustPressed(int keyCode) {
        if (keyCode >= 0 && keyCode < KEY_RANGE && frameJustPressed[keyCode]) {
            frameJustPressed[keyCode] = false; // Clear immediately after reading
            return true;
        }
        return false;
    }

    public void prepareFrame() {
        synchronized (this) {
            long currentTime = System.nanoTime();

            // Process pending press counts
            for (int i = 0; i < KEY_RANGE; i++) {
                if (pendingJustPressedCount[i] > 0) {
                    frameJustPressed[i] = true;
                    pendingJustPressedCount[i] = 0; // Reset count
                }
            }

            // Handle key repeat for held keys
            for (int i = 0; i < KEY_RANGE; i++) {
                if (pressed[i] && pressedTime[i] > 0) {
                    long heldTime = currentTime - pressedTime[i];

                    // Initial delay, then repeat at interval
                    if (heldTime >= REPEAT_DELAY) {
                        long timeSinceDelay = heldTime - REPEAT_DELAY;
                        if (timeSinceDelay % REPEAT_INTERVAL < 16_000_000) { // ~1 frame tolerance
                            frameJustPressed[i] = true;
                        }
                    }
                }
            }
        }
    }

    public synchronized void resetAll() {
        Arrays.fill(pressed, false);
        Arrays.fill(pendingJustPressedCount, 0);
        Arrays.fill(frameJustPressed, false);
    }

    public synchronized void clearFrameJustPressed() {
        Arrays.fill(frameJustPressed, false);
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // not used
    }

    @Override
    public synchronized void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code >= 0 && code < KEY_RANGE) {
            if (!pressed[code]) {
                pendingJustPressedCount[code]++; // Increment counter
                pressedTime[code] = System.nanoTime(); // Start tracking hold time
            }
            pressed[code] = true;
        }
    }

    @Override
    public synchronized void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code >= 0 && code < KEY_RANGE) {
            pressed[code] = false;
            pressedTime[code] = 0; // Reset hold time
        }
    }
}
