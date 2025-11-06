package com.arcade.arkanoid.gameplay.entities;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.LinkedList;
import java.util.Queue;

public class Ball extends AbstractEntity {
    private final Color fillColor;
    private final Color borderColor;
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

    @Override
    public void update(double deltaTime) {
        super.update(deltaTime);
        
        // Add current position to trail if ball is moving
        if (velocity.x != 0 || velocity.y != 0) {
            trail.add(new TrailPoint(position.x + width / 2, position.y + height / 2));
            if (trail.size() > MAX_TRAIL_LENGTH) {
                trail.poll();
            }
        }
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
        graphics.setColor(borderColor);
        graphics.setStroke(new java.awt.BasicStroke(2));
        graphics.drawOval((int) position.x, (int) position.y, (int) width, (int) height);
    }
}
