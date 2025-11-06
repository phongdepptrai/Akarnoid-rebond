package com.arcade.arkanoid.gameplay.system;

import com.arcade.arkanoid.gameplay.levels.LevelDefinition;
import com.arcade.arkanoid.gameplay.objectives.ObjectiveEngine;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.List;

/**
 * Singleton renderer for gameplay side panels with score, lives, level, and
 * mission information.
 * Uses Factory Method pattern for creating panel content.
 */
public class GameplayPanelRenderer {
    private static final int PANEL_WIDTH = 240;
    private static final Color PANEL_BG = new Color(10, 10, 20, 150);
    private static final Color PANEL_BORDER = new Color(255, 140, 0);
    private static final Color PANEL_INNER_BORDER = new Color(180, 100, 0);

    private static final Font TITLE_FONT = new Font("emulogic", Font.PLAIN, 14);
    private static final Font VALUE_FONT = new Font("emulogic", Font.PLAIN, 20);
    private static final Font SMALL_VALUE_FONT = new Font("emulogic", Font.PLAIN, 16);
    private static final Font OBJECTIVE_FONT = new Font("emulogic", Font.PLAIN, 10);

    // Singleton instance
    private static GameplayPanelRenderer instance;

    private GameplayPanelRenderer() {
        // Private constructor for singleton
    }

    /**
     * Gets the singleton instance.
     */
    public static GameplayPanelRenderer getInstance() {
        if (instance == null) {
            instance = new GameplayPanelRenderer();
        }
        return instance;
    }

    public static int getPanelWidth() {
        return PANEL_WIDTH;
    }

    public void render(Graphics2D g, int screenWidth, int screenHeight,
            int score, int lives, LevelDefinition level,
            List<ObjectiveEngine.ObjectiveState> objectives) {
        drawPanel(g, 0, 0, PANEL_WIDTH, screenHeight, true, score, lives);
        drawPanel(g, screenWidth - PANEL_WIDTH, 0, PANEL_WIDTH, screenHeight, false, level, objectives);
    }

    private void drawPanel(Graphics2D g, int x, int y, int w, int h, boolean isLeft, Object... data) {
        // Background
        g.setColor(PANEL_BG);
        g.fillRect(x, y, w, h);

        // Borders
        g.setColor(PANEL_BORDER);
        g.setStroke(new BasicStroke(3));
        g.drawRect(x + 2, y + 2, w - 4, h - 4);

        g.setColor(PANEL_INNER_BORDER);
        g.setStroke(new BasicStroke(2));
        g.drawRect(x + 8, y + 8, w - 16, h - 16);

        drawCorners(g, x, y, w, h);

        if (isLeft) {
            drawLeftContent(g, x, w, (int) data[0], (int) data[1]);
        } else {
            drawRightContent(g, x, w, (LevelDefinition) data[0],
                    (List<ObjectiveEngine.ObjectiveState>) data[1]);
        }
    }

    private void drawCorners(Graphics2D g, int x, int y, int w, int h) {
        int size = 20;
        g.setColor(PANEL_BORDER);
        g.setStroke(new BasicStroke(2));

        // Factory method for corner lines
        drawCornerLine(g, x, y, size, true, true); // Top-left
        drawCornerLine(g, x + w, y, size, false, true); // Top-right
        drawCornerLine(g, x, y + h, size, true, false); // Bottom-left
        drawCornerLine(g, x + w, y + h, size, false, false); // Bottom-right
    }

    /**
     * Factory method for creating corner decorations.
     */
    private void drawCornerLine(Graphics2D g, int x, int y, int size, boolean left, boolean top) {
        int xDir = left ? 1 : -1;
        int yDir = top ? 1 : -1;
        g.drawLine(x, y + yDir * size, x, y);
        g.drawLine(x, y, x + xDir * size, y);
    }

    private void drawLeftContent(Graphics2D g, int x, int w, int score, int lives) {
        int centerX = x + w / 2;
        drawSection(g, centerX, 80, "SCORE", String.valueOf(score), VALUE_FONT);
        drawSection(g, centerX, 200, "LIVES", null, null);
        drawLives(g, centerX, 220, lives);
    }

    private void drawRightContent(Graphics2D g, int x, int w, LevelDefinition level,
            List<ObjectiveEngine.ObjectiveState> objectives) {
        int centerX = x + w / 2;
        String levelName = level != null ? level.displayName() : "---";
        drawSection(g, centerX, 80, "LEVEL", levelName, SMALL_VALUE_FONT);
        drawSection(g, centerX, 180, "MISSION", null, null);
        drawObjectives(g, x, w, 210, objectives);
    }

    /**
     * Factory method for creating a titled section with optional value.
     */
    private void drawSection(Graphics2D g, int centerX, int y, String title, String value, Font valueFont) {
        drawCenteredText(g, title, TITLE_FONT, PANEL_BORDER, centerX, y);
        if (value != null && valueFont != null) {
            int valueY = valueFont == VALUE_FONT ? y + 40 : y + 35;
            drawCenteredText(g, value, valueFont, Color.WHITE, centerX, valueY);
        }
    }

    private void drawLives(Graphics2D g, int centerX, int y, int count) {
        int size = 30, spacing = 10;
        int startX = centerX - (count * size + (count - 1) * spacing) / 2;

        for (int i = 0; i < count; i++) {
            drawLifeHeart(g, startX + i * (size + spacing), y, size);
        }
    }

    /**
     * Factory method for creating individual life heart icon.
     */
    private void drawLifeHeart(Graphics2D g, int x, int y, int size) {
        g.setColor(new Color(255, 50, 50));
        g.fillOval(x, y, size, size);
        g.setColor(new Color(200, 0, 0));
        g.setStroke(new BasicStroke(2));
        g.drawOval(x, y, size, size);
    }

    private void drawObjectives(Graphics2D g, int x, int w, int startY,
            List<ObjectiveEngine.ObjectiveState> objectives) {
        g.setFont(OBJECTIVE_FONT);
        int y = startY;
        for (ObjectiveEngine.ObjectiveState obj : objectives) {
            drawObjectiveLine(g, x, w, y, obj);
            y += 25;
        }
    }

    /**
     * Factory method for creating individual objective line.
     */
    private void drawObjectiveLine(Graphics2D g, int x, int w, int y, ObjectiveEngine.ObjectiveState obj) {
        boolean done = obj.status() == ObjectiveEngine.Status.COMPLETED;
        String status = (done ? "[X] " : "[..] ") + obj.id();
        String progress = obj.progress() + "/" + obj.target();

        g.setColor(done ? new Color(100, 255, 100) : Color.WHITE);
        g.drawString(status, x + 15, y);
        g.setColor(new Color(200, 200, 200));
        g.drawString(progress, x + w - 15 - g.getFontMetrics().stringWidth(progress), y);
    }

    private void drawCenteredText(Graphics2D g, String text, Font font, Color color, int centerX, int y) {
        g.setFont(font);
        g.setColor(color);
        g.drawString(text, centerX - g.getFontMetrics().stringWidth(text) / 2, y);
    }
}
