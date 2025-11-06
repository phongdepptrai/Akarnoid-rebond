package com.arcade.arkanoid.gameplay.entities;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Paddle extends AbstractEntity {
    private final double baseWidth;
    private final double speed;
    private final Color fillColor;
    private final Color borderColor;
    private BufferedImage paddleImage;

    public Paddle(double x, double y, double width, double height, double speed, Color fillColor) {
        this(x, y, width, height, speed, fillColor, fillColor.darker());
    }

    public Paddle(double x,
            double y,
            double width,
            double height,
            double speed,
            Color fillColor,
            Color borderColor) {
        super(x, y, width, height);
        this.speed = speed;
        this.fillColor = fillColor;
        this.borderColor = borderColor;
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

    public void setPaddleImage(BufferedImage image) {
        this.paddleImage = image;
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
        if (paddleImage != null) {
            graphics.drawImage(paddleImage, (int) position.x, (int) position.y, (int) width, (int) height, null);
        }
    }
}
