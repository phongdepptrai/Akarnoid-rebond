package com.arcade.arkanoid;

import com.arcade.arkanoid.engine.core.Game;
import com.arcade.arkanoid.engine.core.GameConfig;
import com.arcade.arkanoid.engine.scene.SceneManager;
import com.arcade.arkanoid.gameplay.GameplayScene;
import com.arcade.arkanoid.menu.MainMenuScene;
import com.arcade.arkanoid.menu.PauseScene;
import com.arcade.arkanoid.menu.ProfileDetailScene;
import com.arcade.arkanoid.menu.worldmap.WorldMapScene;
import com.arcade.arkanoid.menu.save.SaveMenuScene;
import com.arcade.arkanoid.menu.shop.ShopScene;
import com.arcade.arkanoid.menu.settings.SettingsScene;

public class ArkanoidGame extends Game {
    public static final String SCENE_MENU = "menu";
    public static final String SCENE_GAMEPLAY = "gameplay";
    public static final String SCENE_PAUSE = "pause";
    public static final String SCENE_MAP = "map";
    public static final String SCENE_SAVE = "save";
    public static final String SCENE_PROFILE = "profile";
    public static final String SCENE_SHOP = "shop";
    public static final String SCENE_SETTINGS = "settings";

    public ArkanoidGame() {
        super(GameConfig.defaultConfig());
    }

    @Override
    protected void registerScenes(SceneManager sceneManager) {
        sceneManager.register(SCENE_MENU, MainMenuScene::new);
        sceneManager.registerPersistent(SCENE_GAMEPLAY, GameplayScene::new);
        sceneManager.registerPersistent(SCENE_PAUSE, PauseScene::new);
        sceneManager.registerPersistent(SCENE_MAP, WorldMapScene::new);
        sceneManager.registerPersistent(SCENE_SAVE, SaveMenuScene::new);
        sceneManager.register(SCENE_PROFILE, ProfileDetailScene::new);
        sceneManager.registerPersistent(SCENE_SHOP, ShopScene::new);
        sceneManager.register(SCENE_SETTINGS, SettingsScene::new);
    }

    @Override
    protected String initialScene() {
        return SCENE_MENU;
    }
}
