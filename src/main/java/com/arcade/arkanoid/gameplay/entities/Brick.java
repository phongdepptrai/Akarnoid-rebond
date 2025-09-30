package com.arcade.arkanoid.gameplay.entities;

import java.awt.Color;
import java.awt.Graphics2D;

public class Brick extends AbstractEntity {
    private int hitPoints;
    private final Color color;
    private final int scoreValue;

    public Brick(double x, double y, double width, double height, int hitPoints, int scoreValue, Color color) {
        super(x, y, width, height);
        this.hitPoints = hitPoints;
        this.scoreValue = scoreValue;
        this.color = color;
    }

    public void hit() {
        hitPoints = Math.max(0, hitPoints - 1);
    }

    public boolean isDestroyed() {
        return hitPoints <= 0;
    }

    public int getScoreValue() {
        return scoreValue;
    }

    @Override
    public void render(Graphics2D graphics) {
        if (isDestroyed()) {
            return;
        }
        graphics.setColor(color);
        graphics.fillRect((int) position.x, (int) position.y, (int) width, (int) height);
        graphics.setColor(color.darker());
        graphics.drawRect((int) position.x, (int) position.y, (int) width, (int) height);
    }
}
