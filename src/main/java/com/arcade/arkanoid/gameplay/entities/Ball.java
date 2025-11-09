package com.arcade.arkanoid.gameplay.entities;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.LinkedList;
import java.util.Queue;

public class Ball extends AbstractEntity {
    private Color baseFillColor;
    private Color baseBorderColor;
    private Color fillColor;
    private Color borderColor;
    private boolean fireActive;
    private double fireTimer;
    private final Queue<TrailPoint> trail = new LinkedList<>();
    private static final int MAX_TRAIL_LENGTH = 8;
    
    private static class TrailPoint {
        double x, y;
        TrailPoint(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

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
        trail.clear();
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
        if (velocity.x != 0 || velocity.y != 0) {
            trail.add(new TrailPoint(position.x + width / 2, position.y + height / 2));
            if (trail.size() > MAX_TRAIL_LENGTH) {
                trail.poll();
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
        // Draw trail with fading effect
        if (!trail.isEmpty()) {
            int index = 0;
            int size = trail.size();
            for (TrailPoint point : trail) {
                float alpha = (float) index / size * 0.5f; // Fade from 0 to 0.5
                int trailSize = (int) (width * (0.4 + 0.6 * index / size)); // Size from 40% to 100%
                
                graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                graphics.setColor(fillColor);
                graphics.fillOval((int) (point.x - trailSize / 2), (int) (point.y - trailSize / 2), 
                                 trailSize, trailSize);
                index++;
            }
            graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }
        
        // Draw main ball
        graphics.setColor(fillColor);
        graphics.fillOval((int) position.x, (int) position.y, (int) width, (int) height);
        graphics.setColor(fireActive ? Color.WHITE : borderColor);
        graphics.setStroke(new java.awt.BasicStroke(2));
        graphics.drawOval((int) position.x, (int) position.y, (int) width, (int) height);
    }

    private static Color blend(Color a, Color b, double ratio) {
        if (a == null && b == null) {
            return Color.BLACK;
        } else if (a == null) {
            return b;
        } else if (b == null) {
            return a;
        }
        double r = Math.max(0, Math.min(1, ratio));
        int red = (int) (a.getRed() * (1 - r) + b.getRed() * r);
        int green = (int) (a.getGreen() * (1 - r) + b.getGreen() * r);
        int blue = (int) (a.getBlue() * (1 - r) + b.getBlue() * r);
        return new Color(red, green, blue);
    }
}
