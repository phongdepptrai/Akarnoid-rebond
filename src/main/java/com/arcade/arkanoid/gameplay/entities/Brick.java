package com.arcade.arkanoid.gameplay.entities;

import java.awt.Color;
import java.awt.Graphics2D;

public class Brick extends AbstractEntity {
    private int hitPoints;
    private Color color;
    private final int scoreValue;

    public Brick(double x, double y, double width, double height, int hitPoints, int scoreValue) {
        super(x, y, width, height);
        this.hitPoints = Math.max(0, hitPoints);
        this.scoreValue = scoreValue;
        this.color = colorForHitPoints(this.hitPoints);
    }

    public void hit() {
        if (isDestroyed()) {
            return;
        }
        hitPoints = Math.max(0, hitPoints - 1);
        if (!isDestroyed()) {
            color = colorForHitPoints(hitPoints);
        }
    }

    public boolean isDestroyed() {
        return hitPoints <= 0;
    }

    public int getScoreValue() {
        return scoreValue;
    }

    public static Color colorForHitPoints(int strength) {
        int normalized = Math.max(1, strength);
        switch (normalized) {
            case 1:
                return new Color(0xFF7043);
            case 2:
                return new Color(0xFFA000);
            case 3:
                return new Color(0xF44336);
            default:
                return new Color(0x9C27B0);
        }
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
