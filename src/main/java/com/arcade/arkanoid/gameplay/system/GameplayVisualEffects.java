package com.arcade.arkanoid.gameplay.system;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;

/**
 * Singleton class responsible for rendering visual effects in gameplay scene,
 * including the title text and neon border dots.
 */
public class GameplayVisualEffects {
    private static GameplayVisualEffects instance;

    public static final int SIDE_PANEL_WIDTH = GameplayPanelRenderer.getPanelWidth();

    private GameplayVisualEffects() {
        // Private constructor for singleton
    }

    /**
     * Factory method to get singleton instance.
     *
     * @return the singleton instance
     */
    public static GameplayVisualEffects getInstance() {
        if (instance == null) {
            instance = new GameplayVisualEffects();
        }
        return instance;
    }

    /**
     * Factory method to draw 3D styled "ARKANOID" title at top of gameplay scene.
     *
     * @param graphics graphics context
     * @param width    screen width
     */
    public void drawTitle(Graphics2D graphics, int width) {
        Font titleFont = new Font("iomanoid", Font.PLAIN, 120);
        String title = "ARKANOID";

        graphics.setFont(titleFont);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int titleWidth = graphics.getFontMetrics(titleFont).stringWidth(title);
        int titleX = (width - titleWidth) / 2;
        int titleY = 80;

        draw3DSpaceText(graphics, title, titleFont, titleX, titleY);
    }

    /**
     * Factory method to render 3D space-themed text with glow and gradient.
     *
     * @param g    graphics context
     * @param text text to render
     * @param font font to use
     * @param x    x position
     * @param y    y position
     */
    private void draw3DSpaceText(Graphics2D g, String text, Font font, int x, int y) {
        g.setFont(font);

        TextLayout textLayout = new TextLayout(text, font, g.getFontRenderContext());

        // Shadow layers for depth
        for (int depth = 8; depth > 0; depth--) {
            Shape shadowOutline = textLayout.getOutline(AffineTransform.getTranslateInstance(x + depth * 2, y + depth));
            g.setColor(new Color(0, 0, 0, 30));
            g.fill(shadowOutline);
        }

        // Glow effect
        Shape mainOutline = textLayout.getOutline(AffineTransform.getTranslateInstance(x, y));
        for (int glow = 12; glow > 0; glow--) {
            g.setColor(new Color(0, 150, 255, 8));
            g.setStroke(new BasicStroke(glow * 2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(mainOutline);
        }

        // Gradient fill
        LinearGradientPaint spaceGradient = new LinearGradientPaint(
                x, y - 80, x, y + 40,
                new float[] { 0f, 0.3f, 0.6f, 1f },
                new Color[] {
                        new Color(0x00FFFF),
                        new Color(0x0099FF),
                        new Color(0x6633FF),
                        new Color(0x9900CC)
                });

        g.setPaint(spaceGradient);
        g.fill(mainOutline);

        // White outline
        g.setStroke(new BasicStroke(2f));
        g.setColor(new Color(255, 255, 255, 200));
        g.draw(mainOutline);
    }

    /**
     * Factory method to draw neon border around game play area only.
     *
     * @param graphics graphics context
     * @param width    screen width
     * @param height   screen height
     */
    public void drawGameAreaBorder(Graphics2D graphics, int width, int height) {
        // Game area boundaries (between side panels with offset to avoid panel border)
        int leftBound = SIDE_PANEL_WIDTH + 15; // Offset 15px from panel border
        int rightBound = width - SIDE_PANEL_WIDTH - 15; // Offset 15px from panel border
        int topBound = 100; // Lowered to create more space at top
        int bottomBound = height - 10; // Extended to near bottom

        int dotSize = 2; // Larger dots to overlap and stick together
        int spacing = 10; // Smaller spacing so dots touch each other

        // Gradient colors matching title text (cyan → blue → purple → magenta)
        Color neonCyan = new Color(0x00FFFF); // Cyan
        Color neonBlue = new Color(0x0099FF); // Blue
        Color neonPurple = new Color(0x6633FF); // Purple
        Color neonMagenta = new Color(0x9900CC); // Magenta
        Color neonGlow = new Color(0x0099FF, true); // Blue with transparency

        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Top border
        for (int x = leftBound; x <= rightBound; x += spacing) {
            drawNeonDot(graphics, x, topBound, dotSize, neonCyan, neonGlow);
        }

        // Left border
        for (int y = topBound; y <= bottomBound; y += spacing) {
            drawNeonDot(graphics, leftBound, y, dotSize, neonCyan, neonGlow);
        }

        // Right border
        for (int y = topBound; y <= bottomBound; y += spacing) {
            drawNeonDot(graphics, rightBound, y, dotSize, neonCyan, neonGlow);
        }
    }

    /**
     * Factory method to draw a single neon dot with multi-layer glow effect and
     * gradient colors.
     *
     * @param g    graphics context
     * @param x    x position
     * @param y    y position
     * @param size dot size
     * @param core core color (unused, kept for compatibility)
     * @param glow glow color (unused, kept for compatibility)
     */
    private void drawNeonDot(Graphics2D g, int x, int y, int size, Color core, Color glow) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Outer glow layer (largest, most transparent) - purple/magenta tint
        g.setColor(new Color(102, 51, 255, 60)); // Purple glow
        g.fillOval(x - size * 3, y - size * 3, size * 6, size * 6);

        // Middle glow layer - blue tint
        g.setColor(new Color(0, 153, 255, 100)); // Blue glow
        g.fillOval(x - size * 2, y - size * 2, size * 4, size * 4);

        // Inner glow layer - cyan/blue
        g.setColor(new Color(0, 153, 255, 180)); // Blue-cyan glow
        g.fillOval(x - size, y - size, size * 2, size * 2);

        // Core dot with gradient (cyan to magenta like the title)
        int dotRadius = size / 2;
        LinearGradientPaint dotGradient = new LinearGradientPaint(
                x, y - dotRadius * 2, x, y + dotRadius * 2,
                new float[] { 0f, 0.3f, 0.7f, 1f },
                new Color[] {
                        new Color(0x00FFFF), // Cyan
                        new Color(0x0099FF), // Blue
                        new Color(0x6633FF), // Purple
                        new Color(0x9900CC) // Magenta
                });
        g.setPaint(dotGradient);
        g.fillOval(x - size / 2, y - size / 2, size, size);

        // Extra bright center point
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
