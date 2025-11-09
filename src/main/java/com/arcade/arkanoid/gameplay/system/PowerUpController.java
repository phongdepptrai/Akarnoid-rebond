package com.arcade.arkanoid.gameplay.system;

import com.arcade.arkanoid.gameplay.entities.Brick;
import com.arcade.arkanoid.gameplay.entities.Paddle;
import com.arcade.arkanoid.gameplay.entities.PowerUp;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Handles spawning, updating, and rendering of power-ups so GameplayScene stays focused on high-level flow.
 */
public final class PowerUpController {
    private final List<PowerUp> activePowerUps = new ArrayList<>();
    private final Random random;
    private final double dropChance;
    private final double powerUpSize;

    public interface Listener {
        void onCollected(PowerUp.Type type);
    }

    public PowerUpController(Random random, double dropChance, double powerUpSize) {
        this.random = Objects.requireNonNull(random, "random");
        this.dropChance = dropChance;
        this.powerUpSize = powerUpSize;
    }

    public void reset() {
        activePowerUps.clear();
    }

    public void maybeSpawnFrom(Brick brick) {
        if (brick == null || random.nextDouble() > dropChance) {
            return;
        }

        PowerUp.Type[] types = {
                PowerUp.Type.EXPAND_PADDLE,
                PowerUp.Type.SLOW_BALL,
                PowerUp.Type.MULTI_BALL,
                PowerUp.Type.FIRE_BALL,
                PowerUp.Type.PADDLE_GUN,
                PowerUp.Type.EXTRA_LIFE
        };
        PowerUp.Type type = types[random.nextInt(types.length)];

        double spawnX = brick.getPosition().x + brick.getWidth() / 2.0 - powerUpSize / 2.0;
        double spawnY = brick.getPosition().y + brick.getHeight() / 2.0 - powerUpSize / 2.0;
        PowerUp powerUp = new PowerUp(spawnX, spawnY, powerUpSize, type, colorFor(type));
        activePowerUps.add(powerUp);
    }

    public void update(double deltaTime, Paddle paddle, double arenaHeight, Listener listener) {
        if (paddle == null) {
            return;
        }

        Iterator<PowerUp> iterator = activePowerUps.iterator();
        while (iterator.hasNext()) {
            PowerUp powerUp = iterator.next();
            powerUp.update(deltaTime);
            if (powerUp.getPosition().y > arenaHeight) {
                iterator.remove();
                continue;
            }
            if (powerUp.getBounds().intersects(paddle.getBounds())) {
                if (listener != null) {
                    listener.onCollected(powerUp.getType());
                }
                iterator.remove();
            }
        }
    }

    public void render(Graphics2D graphics) {
        for (PowerUp powerUp : activePowerUps) {
            powerUp.render(graphics);
        }
    }

    public boolean isEmpty() {
        return activePowerUps.isEmpty();
    }

    private static Color colorFor(PowerUp.Type type) {
        switch (type) {
            case EXPAND_PADDLE:
                return new Color(0x8BC34A);
            case SLOW_BALL:
                return new Color(0xFFEB3B);
            case MULTI_BALL:
                return new Color(0x7E57C2);
            case FIRE_BALL:
                return new Color(0xFF5722);
            case PADDLE_GUN:
                return new Color(0x03A9F4);
            case EXTRA_LIFE:
                return new Color(0xFFD54F);
            default:
                return Color.WHITE;
        }
    }
}
