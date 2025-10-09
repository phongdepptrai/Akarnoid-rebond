package com.arcade.arkanoid.gameplay.entities;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

public class Brick extends AbstractEntity {
    private int hitPoints;
    private Color color;
    private final int scoreValue;
    private final int gridColumn;
    private final int gridRow;
    private final String blueprintType;
    private final List<String> tags;
    private final List<String> modifiers;

    public Brick(double x, double y, double width, double height, int hitPoints, int scoreValue) {
        this(x, y, width, height, hitPoints, scoreValue, -1, -1, "basic", List.of(), List.of());
    }

    public Brick(double x,
                 double y,
                 double width,
                 double height,
                 int hitPoints,
                 int scoreValue,
                 int gridColumn,
                 int gridRow,
                 String blueprintType,
                 List<String> tags,
                 List<String> modifiers) {
        super(x, y, width, height);
        this.hitPoints = Math.max(0, hitPoints);
        this.scoreValue = scoreValue;
        this.color = colorForHitPoints(this.hitPoints);
        this.gridColumn = gridColumn;
        this.gridRow = gridRow;
        this.blueprintType = blueprintType == null ? "basic" : blueprintType;
        this.tags = tags == null ? List.of() : List.copyOf(tags);
        this.modifiers = modifiers == null ? List.of() : List.copyOf(modifiers);
    }

    public void hit() {
        if (isDestroyed()) {
            return;
        }
        hitPoints = Math.max(0, hitPoints - 1);
        if (!isDestroyed()) {
            color = colorForHitPoints(hitPoints);
        }
    }

    public boolean isDestroyed() {
        return hitPoints <= 0;
    }

    public int getScoreValue() {
        return scoreValue;
    }

    public int getGridColumn() {
        return gridColumn;
    }

    public int getGridRow() {
        return gridRow;
    }

    public String getBlueprintType() {
        return blueprintType;
    }

    public List<String> getTags() {
        return tags;
    }

    public List<String> getModifiers() {
        return modifiers;
    }

    public static Color colorForHitPoints(int strength) {
        int normalized = Math.max(1, strength);
        switch (normalized) {
            case 1:
                return new Color(0xFF7043);
            case 2:
                return new Color(0xFFA000);
            case 3:
                return new Color(0xF44336);
            default:
                return new Color(0x9C27B0);
        }
    }

    @Override
    public void render(Graphics2D graphics) {
        if (isDestroyed()) {
            return;
        }
        graphics.setColor(color);
        graphics.fillRect((int) position.x, (int) position.y, (int) width, (int) height);
        graphics.setColor(color.darker());
        graphics.drawRect((int) position.x, (int) position.y, (int) width, (int) height);
    }
}
