package com.arcade.arkanoid.gameplay.entities;

import java.awt.Color;
import java.awt.Graphics2D;

public class Paddle extends AbstractEntity {
    private final double baseWidth;
    private final double speed;
    private final Color color;

    public Paddle(double x, double y, double width, double height, double speed, Color color) {
        super(x, y, width, height);
        this.speed = speed;
        this.color = color;
        this.baseWidth = width;
    }

    public void moveLeft() {
        velocity.x = -speed;
    }

    public void moveRight() {
        velocity.x = speed;
    }

    public void stop() {
        velocity.x = 0;
    }

    public void resetWidth() {
        this.width = baseWidth;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    @Override
    public void update(double deltaTime) {
        super.update(deltaTime);
    }

    public void clamp(double minX, double maxX) {
        if (position.x < minX) {
            position.x = minX;
        }
        if (position.x + width > maxX) {
            position.x = maxX - width;
        }
    }

    @Override
    public void render(Graphics2D graphics) {
        graphics.setColor(color);
        graphics.fillRoundRect((int) position.x, (int) position.y, (int) width, (int) height, 8, 8);
    }
}
