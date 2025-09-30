package com.arcade.arkanoid.engine.core;

import com.arcade.arkanoid.engine.input.InputManager;

import javax.swing.JFrame;
import javax.swing.WindowConstants;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class GameWindow extends JFrame {
    private final Canvas canvas;
    private final GameConfig config;

    public GameWindow(GameConfig config) {
        super(config.title());
        this.config = config;
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setIgnoreRepaint(true);
        setResizable(false);
        canvas = new Canvas();
        canvas.setBackground(Color.BLACK);
        canvas.setIgnoreRepaint(true);
        Dimension size = new Dimension(config.width(), config.height());
        canvas.setPreferredSize(size);
        canvas.setMinimumSize(size);
        canvas.setMaximumSize(size);
        add(canvas);
        pack();
        setLocationRelativeTo(null);
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public GameConfig getConfig() {
        return config;
    }

    public void attachInputListeners(InputManager inputManager) {
        addKeyListener(inputManager);
        canvas.addKeyListener(inputManager);
        addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                requestCanvasFocus();
            }

            @Override
            public void windowLostFocus(WindowEvent e) {
                inputManager.resetAll();
            }
        });
        canvas.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                requestCanvasFocus();
            }

            @Override
            public void focusLost(FocusEvent e) {
                inputManager.resetAll();
            }
        });
        canvas.setFocusable(true);
        requestCanvasFocus();
    }

    private void requestCanvasFocus() {
        if (!canvas.hasFocus()) {
            canvas.requestFocusInWindow();
        }
    }

    public void showWindow() {
        setVisible(true);
        requestCanvasFocus();
        if (canvas.getBufferStrategy() == null) {
            canvas.createBufferStrategy(3);
        }
    }
}
