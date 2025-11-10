package com.arcade.arkanoid.gameplay.system;

import com.arcade.arkanoid.engine.util.GradientUtils;
import com.arcade.arkanoid.gameplay.levels.LevelDefinition;
import com.arcade.arkanoid.gameplay.objectives.ObjectiveEngine;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Singleton renderer for gameplay side panels with score, lives, level, and
 * mission information.
 * Uses Factory Method pattern for creating panel content.
 */
public class GameplayPanelRenderer {
    private static final int PANEL_WIDTH = 240;
    private static final Font TITLE_FONT = new Font("emulogic", Font.PLAIN, 14);
    private static final Font VALUE_FONT = new Font("emulogic", Font.PLAIN, 20);
    private static final Font SMALL_VALUE_FONT = new Font("emulogic", Font.PLAIN, 16);
    private static final Font OBJECTIVE_FONT = new Font("emulogic", Font.PLAIN, 8);

    // Singleton instance
    private static GameplayPanelRenderer instance;

    // Cached panel background/border for performance
    private BufferedImage cachedPanelBackground;
    private int cachedHeight = -1;

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
        // Create cache if needed
        if (cachedPanelBackground == null || cachedHeight != screenHeight) {
            createCachedPanelBackground(screenHeight);
            cachedHeight = screenHeight;
        }

        // Draw cached backgrounds
        g.drawImage(cachedPanelBackground, 0, 0, null);
        g.drawImage(cachedPanelBackground, screenWidth - PANEL_WIDTH, 0, null);

        // Draw dynamic content on top
        drawPanelContent(g, 0, PANEL_WIDTH, screenHeight, true, score, lives, null, null);
        drawPanelContent(g, screenWidth - PANEL_WIDTH, PANEL_WIDTH, screenHeight, false, 0, 0, level, objectives);
    }

    /**
     * Creates cached panel background with all static visual effects.
     */
    private void createCachedPanelBackground(int h) {
        cachedPanelBackground = new BufferedImage(PANEL_WIDTH, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = cachedPanelBackground.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int x = 0, y = 0, w = PANEL_WIDTH;

        // Background with gradient
        g.setPaint(GradientUtils.createVerticalGradient(x, y, h,
                new float[] { 0f, 0.5f, 1f },
                new Color[] {
                        new Color(0, 20, 40, 100),
                        new Color(0, 30, 60, 90),
                        new Color(0, 20, 40, 100)
                }));
        g.fillRect(x, y, w, h);

        // 3D shadow and glow effects
        GradientUtils.drawShadowLayers(g, x, y, w, h, 3);
        GradientUtils.drawGlowLayers(g, x, y, w, h, 4);

        // Main border with full gradient
        g.setPaint(GradientUtils.createFullGradient(x, y, h));
        g.setStroke(new BasicStroke(3));
        g.drawRect(x + 2, y + 2, w - 4, h - 4);

        // Inner border with short gradient
        g.setPaint(GradientUtils.createShortGradient(x, y, h));
        g.setStroke(new BasicStroke(2));
        g.drawRect(x + 8, y + 8, w - 16, h - 16);

        drawCorners(g, x, y, w, h);
        g.dispose();
    }

    /**
     * Draws dynamic panel content (score, lives, objectives) on top of cached
     * background.
     */
    private void drawPanelContent(Graphics2D g, int x, int w, int h, boolean isLeft,
            int score, int lives, LevelDefinition level, List<ObjectiveEngine.ObjectiveState> objectives) {
        if (isLeft) {
            drawLeftContent(g, x, w, score, lives);
        } else {
            drawRightContent(g, x, w, level, objectives);
        }
    }

    private void drawCorners(Graphics2D g, int x, int y, int w, int h) {
        int size = 20;

        // Glow effect for corners
        for (int glow = 2; glow > 0; glow--) {
            g.setColor(new Color(GradientUtils.GLOW_COLOR.getRed(),
                    GradientUtils.GLOW_COLOR.getGreen(),
                    GradientUtils.GLOW_COLOR.getBlue(), 30));
            g.setStroke(new BasicStroke(3 + glow * 2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            drawAllCornerLines(g, x, y, w, h, size);
        }

        // Main corner lines with short gradient
        g.setPaint(GradientUtils.createShortGradient(x, y, h));
        g.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        drawAllCornerLines(g, x, y, w, h, size);
    }

    /**
     * Helper method to draw all four corner lines.
     */
    private void drawAllCornerLines(Graphics2D g, int x, int y, int w, int h, int size) {
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
        // Title with cyan color
        drawCenteredText(g, title, TITLE_FONT, GradientUtils.CYAN, centerX, y);
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
        String status = (done ? "[X] " : "[.] ") + obj.id();
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
