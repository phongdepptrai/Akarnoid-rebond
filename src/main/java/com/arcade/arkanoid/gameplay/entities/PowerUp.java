package com.arcade.arkanoid.gameplay.entities;

import java.awt.Color;
import java.awt.Graphics2D;

public class PowerUp extends AbstractEntity {
    public enum Type {
        EXPAND_PADDLE,
        SLOW_BALL,
        MULTI_BALL,
        EXTRA_LIFE,
        FIRE_BALL,
        PADDLE_GUN
    }

    private final Type type;
    private final Color color;

    public PowerUp(double x, double y, double size, Type type, Color color) {
        super(x, y, size, size);
        this.type = type;
        this.color = color;
        this.velocity.y = 120;
    }

    public Type getType() {
        return type;
    }

    @Override
    public void render(Graphics2D graphics) {
        graphics.setColor(color);
        graphics.fillOval((int) position.x, (int) position.y, (int) width, (int) height);
        graphics.setColor(Color.WHITE);
        graphics.drawString(symbol(), (int) (position.x + width / 3), (int) (position.y + height * 0.7));
    }

    private String symbol() {
        switch (type) {
            case EXPAND_PADDLE:
                return "E";
            case SLOW_BALL:
                return "S";
            case MULTI_BALL:
                return "M";
            case EXTRA_LIFE:
                return "L";
            case FIRE_BALL:
                return "F";
            case PADDLE_GUN:
                return "G";
            default:
                return "?";
        }
    }
}
