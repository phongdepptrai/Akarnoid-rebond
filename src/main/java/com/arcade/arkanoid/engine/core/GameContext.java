package com.arcade.arkanoid.engine.core;

import com.arcade.arkanoid.engine.audio.SoundManager;
import com.arcade.arkanoid.engine.assets.AssetManager;
import com.arcade.arkanoid.engine.input.InputManager;
import com.arcade.arkanoid.engine.scene.SceneManager;

public final class GameContext {
    private final Game game;
    private final GameWindow window;
    private final InputManager input;
    private final SoundManager sound;
    private final AssetManager assets;
    private final SceneManager scenes;
    private final GameConfig config;

    public GameContext(
            Game game,
            GameWindow window,
            InputManager input,
            SoundManager sound,
            AssetManager assets,
            SceneManager scenes,
            GameConfig config
    ) {
        this.game = game;
        this.window = window;
        this.input = input;
        this.sound = sound;
        this.assets = assets;
        this.scenes = scenes;
        this.config = config;
    }

    public Game getGame() {
        return game;
    }

    public GameWindow getWindow() {
        return window;
    }

    public InputManager getInput() {
        return input;
    }

    public SoundManager getSound() {
        return sound;
    }

    public AssetManager getAssets() {
        return assets;
    }

    public SceneManager getScenes() {
        return scenes;
    }

    public GameConfig getConfig() {
        return config;
    }
}
