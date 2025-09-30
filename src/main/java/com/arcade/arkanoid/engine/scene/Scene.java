package com.arcade.arkanoid.engine.scene;

import com.arcade.arkanoid.engine.core.GameContext;

import java.awt.Graphics2D;

public abstract class Scene {
    protected final GameContext context;

    protected Scene(GameContext context) {
        this.context = context;
    }

    public void onEnter() {
    }

    public void onExit() {
    }

    public abstract void update(double deltaTime);

    public abstract void render(Graphics2D graphics);
}
