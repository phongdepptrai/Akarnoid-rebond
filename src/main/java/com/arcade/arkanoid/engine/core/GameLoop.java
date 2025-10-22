package com.arcade.arkanoid.engine.core;

import com.arcade.arkanoid.engine.scene.SceneManager;

import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.image.BufferStrategy;

public class GameLoop implements Runnable {
    private final GameConfig config;
    private final GameContext context;
    private final SceneManager scenes;
    private final GameWindow window;
    private Thread loopThread;
    private volatile boolean running;

    public GameLoop(GameConfig config, GameContext context, SceneManager scenes, GameWindow window) {
        this.config = config;
        this.context = context;
        this.scenes = scenes;
        this.window = window;
    }

    public void start() {
        if (running) {
            return;
        }
        running = true;
        loopThread = new Thread(this, "game-loop");
        loopThread.start();
    }

    public void stop() {
        running = false;
        if (loopThread != null) {
            try {
                loopThread.join();
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void run() {
        final double targetFrameTime = 1_000_000_000.0 / config.targetFps();
        long previousTime = System.nanoTime();
        double accumulator = 0;
        final int MAX_UPDATES_PER_FRAME = 5; // Prevent spiral of death

        while (running) {
            long currentTime = System.nanoTime();
            double elapsed = currentTime - previousTime;
            previousTime = currentTime;
            accumulator += elapsed;

            // Prepare frame ONCE at the start - copy pending to frame
            context.getInput().prepareFrame();

            // Process accumulated time with fixed time steps
            int updateCount = 0;
            while (accumulator >= targetFrameTime && updateCount < MAX_UPDATES_PER_FRAME) {
                double deltaSeconds = targetFrameTime / 1_000_000_000.0;
                scenes.update(deltaSeconds);
                accumulator -= targetFrameTime;
                updateCount++;
            }

            // If we hit max updates, reset accumulator to prevent spiral of death
            if (updateCount >= MAX_UPDATES_PER_FRAME) {
                accumulator = 0;
            }

            render();

            // Yield to prevent CPU spinning
            try {
                Thread.sleep(0, 100000); // Sleep 0.1ms instead of 1ms for better timing
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void render() {
        BufferStrategy strategy = window.getCanvas().getBufferStrategy();
        if (strategy == null) {
            window.getCanvas().createBufferStrategy(3);
            return;
        }

        do {
            do {
                Graphics2D graphics = (Graphics2D) strategy.getDrawGraphics();
                try {
                    graphics.clearRect(0, 0, config.width(), config.height());
                    scenes.render(graphics);
                } finally {
                    graphics.dispose();
                }
            } while (strategy.contentsRestored());

            strategy.show();
        } while (strategy.contentsLost());

        Toolkit.getDefaultToolkit().sync();
    }
}
