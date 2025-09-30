package com.arcade.arkanoid.engine.core;

import com.arcade.arkanoid.engine.assets.AssetManager;
import com.arcade.arkanoid.engine.audio.SoundManager;
import com.arcade.arkanoid.engine.input.InputManager;
import com.arcade.arkanoid.engine.scene.SceneManager;

public abstract class Game {
    private final GameConfig config;
    private final GameWindow window;
    private final InputManager inputManager;
    private final SoundManager soundManager;
    private final AssetManager assetManager;
    private final SceneManager sceneManager;
    private final GameContext context;
    private final GameLoop loop;

    protected Game(GameConfig config) {
        this.config = config;
        this.window = new GameWindow(config);
        this.inputManager = new InputManager();
        this.soundManager = new SoundManager();
        this.assetManager = new AssetManager();
        this.sceneManager = new SceneManager();
        this.context = new GameContext(
                this,
                window,
                inputManager,
                soundManager,
                assetManager,
                sceneManager,
                config
        );
        this.window.attachInputListeners(inputManager);
        this.sceneManager.bindContext(context);
        registerScenes(sceneManager);
        this.loop = new GameLoop(config, context, sceneManager, window);
    }

    public void start() {
        window.showWindow();
        sceneManager.switchTo(initialScene());
        loop.start();
    }

    public void stop() {
        loop.stop();
        window.dispose();
    }

    protected abstract void registerScenes(SceneManager sceneManager);

    protected abstract String initialScene();

    public GameContext getContext() {
        return context;
    }

    public GameConfig getConfig() {
        return config;
    }
}
