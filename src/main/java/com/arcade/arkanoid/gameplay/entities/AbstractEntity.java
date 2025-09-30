package com.arcade.arkanoid.gameplay.entities;

import com.arcade.arkanoid.engine.util.Vector2D;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

public abstract class AbstractEntity {
    protected final Vector2D position;
    protected final Vector2D velocity;
    protected double width;
    protected double height;

    protected AbstractEntity(double x, double y, double width, double height) {
        this.position = new Vector2D(x, y);
        this.velocity = new Vector2D();
        this.width = width;
        this.height = height;
    }

    public void update(double deltaTime) {
        position.x += velocity.x * deltaTime;
        position.y += velocity.y * deltaTime;
    }

    public abstract void render(Graphics2D graphics);

    public Rectangle2D getBounds() {
        return new Rectangle2D.Double(position.x, position.y, width, height);
    }

    public Vector2D getPosition() {
        return position;
    }

    public Vector2D getVelocity() {
        return velocity;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }
}
