package com.arcade.arkanoid;

import com.arcade.arkanoid.engine.core.Game;
import com.arcade.arkanoid.engine.core.GameConfig;
import com.arcade.arkanoid.engine.scene.SceneManager;
import com.arcade.arkanoid.gameplay.GameplayScene;
import com.arcade.arkanoid.menu.MainMenuScene;
import com.arcade.arkanoid.menu.PauseScene;

public class ArkanoidGame extends Game {
    public static final String SCENE_MENU = "menu";
    public static final String SCENE_GAMEPLAY = "gameplay";
    public static final String SCENE_PAUSE = "pause";

    public ArkanoidGame() {
        super(GameConfig.defaultConfig());
    }

    @Override
    protected void registerScenes(SceneManager sceneManager) {
        sceneManager.register(SCENE_MENU, MainMenuScene::new);
        sceneManager.registerPersistent(SCENE_GAMEPLAY, GameplayScene::new);
        sceneManager.registerPersistent(SCENE_PAUSE, PauseScene::new);
    }

    @Override
    protected String initialScene() {
        return SCENE_MENU;
    }
}
