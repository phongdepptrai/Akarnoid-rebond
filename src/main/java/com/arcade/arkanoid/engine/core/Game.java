package com.arcade.arkanoid.engine.core;

import com.arcade.arkanoid.engine.assets.AssetManager;
import com.arcade.arkanoid.engine.audio.SoundManager;
import com.arcade.arkanoid.engine.input.InputManager;
import com.arcade.arkanoid.engine.scene.SceneManager;
import com.arcade.arkanoid.engine.util.IOThreadPool;
import com.arcade.arkanoid.economy.EconomyService;
import com.arcade.arkanoid.engine.settings.SettingsManager;
import com.arcade.arkanoid.localization.LocalizationService;
import com.arcade.arkanoid.profile.ProfileManager;

/**
 * Base Game class with 4-thread architecture:
 * 1. Main Thread (UI/EDT) - Graphics & Input
 * 2. Game Loop Thread (60 FPS) - Update & Collision
 * 3. Audio Thread Pool - Sound effects & Background music
 * 4. I/O Thread - Save/Load & Resource loading
 */
public abstract class Game {
    private final GameConfig config;
    private final GameWindow window;
    private final InputManager inputManager;
    private final SoundManager soundManager;
    private final AssetManager assetManager;
    private final SceneManager sceneManager;
    private final ProfileManager profileManager;
    private final EconomyService economyService;
    private final SettingsManager settingsManager;
    private final LocalizationService localizationService;
    private final GameContext context;
    private final GameLoop loop;

    protected Game(GameConfig config) {
        this.config = config;
        this.window = new GameWindow(config);
        this.inputManager = new InputManager();
        this.soundManager = new SoundManager();
        this.assetManager = new AssetManager();
        this.sceneManager = new SceneManager();
        this.profileManager = new ProfileManager();
        this.economyService = new EconomyService(profileManager);
        this.settingsManager = new SettingsManager();
        this.localizationService = new LocalizationService(settingsManager);
        this.context = new GameContext(
                this,
                window,
                inputManager,
                soundManager,
                assetManager,
                sceneManager,
                config,
                profileManager,
                economyService,
                settingsManager,
                localizationService);
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
        // Save profile synchronously before shutdown
        profileManager.saveProfileSync();

        // Shutdown all threads
        loop.stop();
        soundManager.dispose();
        IOThreadPool.getInstance().shutdown();
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

    public ProfileManager getProfileManager() {
        return profileManager;
    }

    public EconomyService getEconomyService() {
        return economyService;
    }

    public SettingsManager getSettingsManager() {
        return settingsManager;
    }

    public LocalizationService getLocalizationService() {
        return localizationService;
    }
}
