package com.arcade.arkanoid.menu.worldmap;

import com.arcade.arkanoid.gameplay.levels.LevelDefinition;

import java.awt.Rectangle;

public class LevelNode {
    private final LevelDefinition levelDefinition;
    private final int index;
    private final Rectangle bounds;
    private boolean unlocked;
    private boolean completed;
    private String gateMessage;

    public LevelNode(LevelDefinition levelDefinition, int index, Rectangle bounds) {
        this.levelDefinition = levelDefinition;
        this.index = index;
        this.bounds = bounds;
    }

    public LevelDefinition getLevelDefinition() {
        return levelDefinition;
    }

    public int getIndex() {
        return index;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String getGateMessage() {
        return gateMessage;
    }

    public void setGateMessage(String gateMessage) {
        this.gateMessage = gateMessage;
    }
}
