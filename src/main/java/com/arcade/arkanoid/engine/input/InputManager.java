package com.arcade.arkanoid.engine.input;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;

public class InputManager implements KeyListener {
    private static final int KEY_RANGE = 512;
    private final boolean[] pressed = new boolean[KEY_RANGE];
    private final boolean[] pendingJustPressed = new boolean[KEY_RANGE];
    private final boolean[] frameJustPressed = new boolean[KEY_RANGE];

    public synchronized boolean isKeyPressed(int keyCode) {
        return keyCode >= 0 && keyCode < KEY_RANGE && pressed[keyCode];
    }

    public synchronized boolean isKeyJustPressed(int keyCode) {
        return keyCode >= 0 && keyCode < KEY_RANGE && frameJustPressed[keyCode];
    }

    public void prepareFrame() {
        synchronized (this) {
            System.arraycopy(pendingJustPressed, 0, frameJustPressed, 0, KEY_RANGE);
            Arrays.fill(pendingJustPressed, false);
        }
    }

    public synchronized void resetAll() {
        Arrays.fill(pressed, false);
        Arrays.fill(pendingJustPressed, false);
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
                pendingJustPressed[code] = true;
            }
            pressed[code] = true;
        }
    }

    @Override
    public synchronized void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code >= 0 && code < KEY_RANGE) {
            pressed[code] = false;
        }
    }
}


