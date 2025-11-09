package com.arcade.arkanoid.gameplay.system;

import com.arcade.arkanoid.engine.util.GradientUtils;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * Singleton class responsible for rendering visual effects in gameplay scene,
 * including the title text and neon border dots.
 */
public class GameplayVisualEffects {
    private static GameplayVisualEffects instance;
    private BufferedImage cachedVisuals;
    private int cachedWidth = -1;
    private int cachedHeight = -1;

    public static final int SIDE_PANEL_WIDTH = GameplayPanelRenderer.getPanelWidth();

    private GameplayVisualEffects() {
    }

    public static GameplayVisualEffects getInstance() {
        if (instance == null) {
            instance = new GameplayVisualEffects();
        }
        return instance;
    }

    public void drawTitle(Graphics2D graphics, int width) {
        // Title is now part of cached visuals, this method does nothing
    }

    public void drawGameAreaBorder(Graphics2D graphics, int width, int height) {
        if (cachedVisuals == null || cachedWidth != width || cachedHeight != height) {
            createCachedVisuals(width, height);
            cachedWidth = width;
            cachedHeight = height;
        }
        graphics.drawImage(cachedVisuals, 0, 0, null);
    }

    private void createCachedVisuals(int width, int height) {
        cachedVisuals = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = cachedVisuals.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Draw title
        drawTitleToCache(g, width);

        // Draw border
        drawBorderToCache(g, width, height);

        g.dispose();
    }

    private void drawTitleToCache(Graphics2D g, int width) {
        Font titleFont = new Font("iomanoid", Font.PLAIN, 120);
        String title = "ARKANOID";

        int titleWidth = g.getFontMetrics(titleFont).stringWidth(title);
        int titleX = (width - titleWidth) / 2;
        int titleY = 80;

        // Use GradientUtils for optimized 3D text rendering
        GradientUtils.draw3DSpaceTextOptimized(g, title, titleFont, titleX, titleY);
    }

    private void drawBorderToCache(Graphics2D g, int width, int height) {
        int leftBound = SIDE_PANEL_WIDTH + 15;
        int rightBound = width - SIDE_PANEL_WIDTH - 15;
        int topBound = 100;
        int bottomBound = height - 10;
        int dotSize = 3;
        int spacing = 20;

        for (int x = leftBound; x <= rightBound; x += spacing) {
            drawSimpleDot(g, x, topBound, dotSize);
        }

        for (int y = topBound; y <= bottomBound; y += spacing) {
            drawSimpleDot(g, leftBound, y, dotSize);
        }

        for (int y = topBound; y <= bottomBound; y += spacing) {
            drawSimpleDot(g, rightBound, y, dotSize);
        }
    }

    private void drawSimpleDot(Graphics2D g, int x, int y, int size) {
        // Outer glow
        g.setColor(new Color(GradientUtils.BLUE.getRed(),
                GradientUtils.BLUE.getGreen(),
                GradientUtils.BLUE.getBlue(), 60));
        g.fillOval(x - size, y - size, size * 2, size * 2);

        // Middle layer
        g.setColor(new Color(GradientUtils.CYAN.getRed(),
                GradientUtils.CYAN.getGreen(),
                GradientUtils.CYAN.getBlue(), 180));
        g.fillOval(x - size / 2, y - size / 2, size, size);

        // Core
        g.setColor(Color.WHITE);
        g.fillOval(x - 1, y - 1, 2, 2);
    }

    /**
     * Get the left boundary of the game area.
     *
     * @return left boundary x coordinate
     */
    public int getLeftBound() {
        return SIDE_PANEL_WIDTH + 15;
    }

    /**
     * Get the right boundary of the game area.
     *
     * @param width screen width
     * @return right boundary x coordinate
     */
    public int getRightBound(int width) {
        return width - SIDE_PANEL_WIDTH - 15;
    }

    /**
     * Get the top boundary of the game area.
     *
     * @return top boundary y coordinate
     */
    public int getTopBound() {
        return 100;
    }

    /**
     * Get the bottom boundary of the game area.
     *
     * @param height screen height
     * @return bottom boundary y coordinate
     */
    public int getBottomBound(int height) {
        return height - 10;
    }
}
