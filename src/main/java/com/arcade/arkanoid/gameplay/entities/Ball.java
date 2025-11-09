package com.arcade.arkanoid.gameplay.entities;

import java.awt.Color;
import java.awt.Graphics2D;

public class Ball extends AbstractEntity {
    private Color baseFillColor;
    private Color baseBorderColor;
    private Color fillColor;
    private Color borderColor;
    private boolean fireActive;
    private double fireTimer;

    public Ball(double x, double y, double size, Color color) {
        this(x, y, size, color, color.darker());
    }

    public Ball(double x, double y, double size, Color fillColor, Color borderColor) {
        super(x, y, size, size);
        this.baseFillColor = fillColor;
        this.baseBorderColor = borderColor;
        this.fillColor = fillColor;
        this.borderColor = borderColor;
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

    public void update(double deltaTime) {
        super.update(deltaTime);
        if (fireActive) {
            fireTimer -= deltaTime;
            if (fireTimer <= 0) {
                fireActive = false;
                fillColor = baseFillColor;
                borderColor = baseBorderColor;
            }
        }
    }

    public void setBaseColors(Color fill, Color border) {
        this.baseFillColor = fill;
        this.baseBorderColor = border;
        if (!fireActive) {
            this.fillColor = fill;
            this.borderColor = border;
        }
    }

    public void setFire(double durationSeconds) {
        fireActive = true;
        fireTimer = Math.max(fireTimer, durationSeconds);
        fillColor = blend(baseFillColor, Color.ORANGE, 0.6);
        borderColor = Color.ORANGE.darker();
    }

    public void clearFire() {
        fireActive = false;
        fireTimer = 0;
        fillColor = baseFillColor;
        borderColor = baseBorderColor;
    }

    public boolean isFireActive() {
        return fireActive;
    }

    public Ball duplicate() {
        Ball copy = new Ball(position.x, position.y, width, baseFillColor, baseBorderColor);
        copy.setVelocity(velocity.x, velocity.y);
        if (fireActive) {
            copy.setFire(fireTimer);
        }
        return copy;
    }

    public Color getFillColor() {
        return baseFillColor;
    }

    public Color getBorderColor() {
        return baseBorderColor;
    }

    @Override
    public void render(Graphics2D graphics) {
        graphics.setColor(fireActive ? blend(fillColor, Color.WHITE, 0.4) : fillColor);
        graphics.fillOval((int) position.x, (int) position.y, (int) width, (int) height);
        graphics.setColor(fireActive ? Color.WHITE : borderColor);
        graphics.setStroke(new java.awt.BasicStroke(2));
        graphics.drawOval((int) position.x, (int) position.y, (int) width, (int) height);
    }

    private static Color blend(Color a, Color b, double ratio) {
        double r = Math.max(0, Math.min(1, ratio));
        int red = (int) (a.getRed() * (1 - r) + b.getRed() * r);
        int green = (int) (a.getGreen() * (1 - r) + b.getGreen() * r);
        int blue = (int) (a.getBlue() * (1 - r) + b.getBlue() * r);
        return new Color(red, green, blue);
    }
}
