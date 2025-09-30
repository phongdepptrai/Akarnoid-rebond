package com.arcade.arkanoid.gameplay.entities;

import java.awt.Color;
import java.awt.Graphics2D;

public class Ball extends AbstractEntity {
    private final Color color;

    public Ball(double x, double y, double size, Color color) {
        super(x, y, size, size);
        this.color = color;
    }

    public void setVelocity(double vx, double vy) {
        this.velocity.x = vx;
        this.velocity.y = vy;
    }

    public void invertX() {
        velocity.x = -velocity.x;
    }

    public void invertY() {
        velocity.y = -velocity.y;
    }

    public void resetPosition(double x, double y) {
        position.x = x;
        position.y = y;
        velocity.x = 0;
        velocity.y = 0;
    }

    @Override
    public void render(Graphics2D graphics) {
        graphics.setColor(color);
        graphics.fillOval((int) position.x, (int) position.y, (int) width, (int) height);
    }
}
