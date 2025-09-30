package com.arcade.arkanoid.engine.util;

public final class Vector2D {
    public double x;
    public double y;

    public Vector2D() {
        this(0, 0);
    }

    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vector2D copy() {
        return new Vector2D(x, y);
    }

    public void add(Vector2D other) {
        this.x += other.x;
        this.y += other.y;
    }

    public void multiply(double scalar) {
        this.x *= scalar;
        this.y *= scalar;
    }

    public double length() {
        return Math.sqrt(x * x + y * y);
    }

    public void normalize() {
        double length = length();
        if (length == 0) {
            return;
        }
        this.x /= length;
        this.y /= length;
    }
}
