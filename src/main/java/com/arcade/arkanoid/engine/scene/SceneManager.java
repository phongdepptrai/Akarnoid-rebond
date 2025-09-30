package com.arcade.arkanoid.engine.scene;

import com.arcade.arkanoid.engine.core.GameContext;

import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class SceneManager {
    private static final class SceneEntry {
        final Function<GameContext, Scene> factory;
        final boolean persistent;
        Scene cachedInstance;

        SceneEntry(Function<GameContext, Scene> factory, boolean persistent) {
            this.factory = factory;
            this.persistent = persistent;
        }
    }

    private final Map<String, SceneEntry> sceneEntries = new HashMap<>();
    private GameContext context;
    private Scene activeScene;
    private String activeSceneId;

    public void bindContext(GameContext context) {
        this.context = context;
    }

    public void register(String id, Function<GameContext, Scene> factory) {
        register(id, factory, false);
    }

    public void registerPersistent(String id, Function<GameContext, Scene> factory) {
        register(id, factory, true);
    }

    private void register(String id, Function<GameContext, Scene> factory, boolean persistent) {
        sceneEntries.put(Objects.requireNonNull(id), new SceneEntry(Objects.requireNonNull(factory), persistent));
    }

    public void switchTo(String id) {
        SceneEntry entry = sceneEntries.get(id);
        if (entry == null) {
            throw new IllegalArgumentException("No scene registered for id: " + id);
        }

        if (activeScene != null) {
            activeScene.onExit();
        }

        if (entry.persistent) {
            if (entry.cachedInstance == null) {
                entry.cachedInstance = entry.factory.apply(context);
            }
            activeScene = entry.cachedInstance;
        } else {
            activeScene = entry.factory.apply(context);
        }

        activeSceneId = id;
        activeScene.onEnter();
    }

    public void update(double deltaTime) {
        if (activeScene != null) {
            activeScene.update(deltaTime);
        }
    }

    public void render(Graphics2D graphics) {
        if (activeScene != null) {
            activeScene.render(graphics);
        }
    }

    public Scene getActiveScene() {
        return activeScene;
    }

    public String getActiveSceneId() {
        return activeSceneId;
    }

    public Scene getPersistentScene(String id) {
        SceneEntry entry = sceneEntries.get(id);
        if (entry == null || !entry.persistent) {
            return null;
        }
        if (entry.cachedInstance == null) {
            entry.cachedInstance = entry.factory.apply(context);
        }
        return entry.cachedInstance;
    }
}
