package com.arcade.arkanoid.gameplay.system;

import com.arcade.arkanoid.gameplay.entities.AbstractEntity;
import com.arcade.arkanoid.gameplay.entities.Brick;
import com.arcade.arkanoid.gameplay.entities.Paddle;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

/**
 * Encapsulates paddle-mounted weapon behaviour, including bullet lifecycle and rendering.
 */
public final class PaddleGunSystem {
    private static final double DEFAULT_DURATION_SECONDS = 8.0;
    private static final double FIRE_INTERVAL_SECONDS = 0.25;
    private static final double LEFT_OFFSET = 10.0;
    private static final double RIGHT_OFFSET = 16.0;
    private static final double ORIGIN_Y_OFFSET = 10.0;
    private static final double BULLET_TOP_BOUNDARY = 50.0;
    private static final double TOP_BOUNDARY = 40.0;

    private final List<Bullet> bullets = new ArrayList<>();
    private double timer;
    private double cooldown;
    private boolean active;

    public void reset() {
        bullets.clear();
        timer = 0.0;
        cooldown = 0.0;
        active = false;
    }

    public void activate() {
        active = true;
        timer = DEFAULT_DURATION_SECONDS;
        cooldown = 0.0;
    }

    public boolean isActive() {
        return active;
    }

    public void update(double deltaTime, Paddle paddle, List<Brick> bricks, Consumer<Brick> onBrickDestroyed) {
        if (!active) {
            return;
        }
        timer -= deltaTime;
        cooldown -= deltaTime;
        if (timer <= 0) {
            reset();
            return;
        }
        if (cooldown <= 0 && paddle != null) {
            fireBurst(paddle);
            cooldown = FIRE_INTERVAL_SECONDS;
        }
        updateBullets(deltaTime, bricks, onBrickDestroyed);
    }

    public void render(Graphics2D graphics) {
        for (Bullet bullet : bullets) {
            bullet.render(graphics);
        }
    }

    private void fireBurst(Paddle paddle) {
        double leftX = paddle.getPosition().x + LEFT_OFFSET;
        double rightX = paddle.getPosition().x + paddle.getWidth() - RIGHT_OFFSET;
        double originY = paddle.getPosition().y - ORIGIN_Y_OFFSET;
        bullets.add(new Bullet(leftX, originY));
        bullets.add(new Bullet(rightX, originY));
    }

    private void updateBullets(double deltaTime, List<Brick> bricks, Consumer<Brick> onBrickDestroyed) {
        Iterator<Bullet> iterator = bullets.iterator();
        while (iterator.hasNext()) {
            Bullet bullet = iterator.next();
            bullet.update(deltaTime);
            if (bullet.getPosition().y + bullet.getHeight() < TOP_BOUNDARY) {
                iterator.remove();
                continue;
            }

            boolean hit = false;
            if (bricks != null) {
                for (Brick brick : bricks) {
                    if (brick.isDestroyed()) {
                        continue;
                    }
                    if (bullet.getBounds().intersects(brick.getBounds())) {
                        brick.hit();
                        if (brick.isDestroyed() && onBrickDestroyed != null) {
                            onBrickDestroyed.accept(brick);
                        }
                        iterator.remove();
                        hit = true;
                        break;
                    }
                }
            }
            if (!hit && bullet.getPosition().y < TOP_BOUNDARY) {
                iterator.remove();
            }
            }
        }
    }

    private static final class Bullet extends AbstractEntity {
        private static final Color BODY_COLOR = new Color(0xFFE082);
        private static final Color BORDER_COLOR = new Color(0xFFB74D);

        Bullet(double x, double y) {
            super(x, y, 6, 16);
            this.velocity.y = -420;
        }

        @Override
        public void render(Graphics2D graphics) {
            graphics.setColor(BODY_COLOR);
            graphics.fillRoundRect((int) position.x, (int) position.y, (int) width, (int) height, 4, 4);
            graphics.setColor(BORDER_COLOR);
            graphics.setStroke(new BasicStroke(2));
            graphics.drawRoundRect((int) position.x, (int) position.y, (int) width, (int) height, 4, 4);
        }
    }
}
