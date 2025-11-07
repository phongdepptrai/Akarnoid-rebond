package com.arcade.arkanoid.engine.util;

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
 * Utility class for creating gradient effects and 3D text rendering.
 * Provides reusable methods for consistent visual styling across the
 * application.
 */
public class GradientUtils {

    // Gradient color constants - Cyan to Magenta spectrum
    public static final Color[] FULL_GRADIENT_COLORS = {
            new Color(0x00FFFF), // Cyan
            new Color(0x0099FF), // Blue
            new Color(0x6633FF), // Purple
            new Color(0x9900CC) // Magenta
    };
    public static final float[] FULL_GRADIENT_FRACTIONS = { 0f, 0.3f, 0.6f, 1f };

    public static final Color[] SHORT_GRADIENT_COLORS = {
            new Color(0x00FFFF), // Cyan
            new Color(0x0099FF), // Blue
            new Color(0x6633FF) // Purple
    };
    public static final float[] SHORT_GRADIENT_FRACTIONS = { 0f, 0.5f, 1f };

    // Common colors
    public static final Color CYAN = new Color(0x00FFFF);
    public static final Color BLUE = new Color(0x0099FF);
    public static final Color PURPLE = new Color(0x6633FF);
    public static final Color MAGENTA = new Color(0x9900CC);
    public static final Color GLOW_COLOR = new Color(0, 200, 255);

    private GradientUtils() {
        // Utility class, no instantiation
    }

    /**
     * Creates a vertical gradient paint.
     * 
     * @param x         x coordinate of gradient start
     * @param y         y coordinate of gradient start
     * @param height    height of gradient
     * @param fractions array of fraction values
     * @param colors    array of colors
     * @return LinearGradientPaint object
     */
    public static LinearGradientPaint createVerticalGradient(int x, int y, int height,
            float[] fractions, Color[] colors) {
        return new LinearGradientPaint(x, y, x, y + height, fractions, colors);
    }

    /**
     * Creates a full 4-color gradient (Cyan → Blue → Purple → Magenta).
     * 
     * @param x      x coordinate of gradient start
     * @param y      y coordinate of gradient start
     * @param height height of gradient
     * @return LinearGradientPaint with full color spectrum
     */
    public static LinearGradientPaint createFullGradient(int x, int y, int height) {
        return createVerticalGradient(x, y, height, FULL_GRADIENT_FRACTIONS, FULL_GRADIENT_COLORS);
    }

    /**
     * Creates a short 3-color gradient (Cyan → Blue → Purple).
     * 
     * @param x      x coordinate of gradient start
     * @param y      y coordinate of gradient start
     * @param height height of gradient
     * @return LinearGradientPaint with short color spectrum
     */
    public static LinearGradientPaint createShortGradient(int x, int y, int height) {
        return createVerticalGradient(x, y, height, SHORT_GRADIENT_FRACTIONS, SHORT_GRADIENT_COLORS);
    }

    /**
     * Draws shadow layers for 3D effect.
     * 
     * @param g      graphics context
     * @param x      x coordinate
     * @param y      y coordinate
     * @param width  width of shape
     * @param height height of shape
     * @param layers number of shadow layers
     */
    public static void drawShadowLayers(Graphics2D g, int x, int y, int width, int height, int layers) {
        for (int depth = layers; depth > 0; depth--) {
            g.setColor(new Color(0, 0, 0, 30));
            g.setStroke(new BasicStroke(3 + depth));
            g.drawRect(x + 2 + depth, y + 2 + depth, width - 4, height - 4);
        }
    }

    /**
     * Draws glow layers around a rectangle.
     * 
     * @param g      graphics context
     * @param x      x coordinate
     * @param y      y coordinate
     * @param width  width of shape
     * @param height height of shape
     * @param layers number of glow layers
     */
    public static void drawGlowLayers(Graphics2D g, int x, int y, int width, int height, int layers) {
        for (int glow = layers; glow > 0; glow--) {
            g.setColor(new Color(GLOW_COLOR.getRed(), GLOW_COLOR.getGreen(), GLOW_COLOR.getBlue(), 20));
            g.setStroke(new BasicStroke((3 + glow * 2f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawRect(x + 2, y + 2, width - 4, height - 4);
        }
    }

    /**
     * Draws 3D space text with shadow, glow, and gradient effects.
     * Creates futuristic sci-fi styled text with depth and neon effects.
     * 
     * @param g            graphics context
     * @param text         text to render
     * @param font         font to use
     * @param x            x position
     * @param y            y position
     * @param shadowLayers number of shadow layers (recommended: 3-8)
     * @param glowLayers   number of glow layers (recommended: 4-12)
     */
    public static void draw3DSpaceText(Graphics2D g, String text, Font font, int x, int y,
            int shadowLayers, int glowLayers) {
        g.setFont(font);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        TextLayout textLayout = new TextLayout(text, font, g.getFontRenderContext());

        // Draw shadow layers
        int shadowAlpha = shadowLayers <= 3 ? 50 : 30;
        for (int depth = shadowLayers; depth > 0; depth--) {
            Shape shadowOutline = textLayout
                    .getOutline(AffineTransform.getTranslateInstance(x + depth * 2, y + depth));
            g.setColor(new Color(0, 0, 0, shadowAlpha));
            g.fill(shadowOutline);
        }

        // Draw glow layers
        Shape mainOutline = textLayout.getOutline(AffineTransform.getTranslateInstance(x, y));
        int glowAlpha = glowLayers <= 4 ? 15 : 8;
        float glowStrokeWidth = glowLayers <= 4 ? 3f : 2f;
        for (int glow = glowLayers; glow > 0; glow--) {
            g.setColor(new Color(0, 150, 255, glowAlpha));
            g.setStroke(new BasicStroke(glow * glowStrokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(mainOutline);
        }

        // Fill with gradient
        LinearGradientPaint gradient = new LinearGradientPaint(
                x, y - 80, x, y + 40,
                FULL_GRADIENT_FRACTIONS,
                FULL_GRADIENT_COLORS);
        g.setPaint(gradient);
        g.fill(mainOutline);

        // Draw outline
        g.setStroke(new BasicStroke(2f));
        g.setColor(new Color(255, 255, 255, 200));
        g.draw(mainOutline);
    }

    /**
     * Draws 3D space text with default settings (3 shadow layers, 4 glow layers).
     * Optimized version for performance-critical rendering.
     * 
     * @param g    graphics context
     * @param text text to render
     * @param font font to use
     * @param x    x position
     * @param y    y position
     */
    public static void draw3DSpaceTextOptimized(Graphics2D g, String text, Font font, int x, int y) {
        draw3DSpaceText(g, text, font, x, y, 3, 4);
    }

    /**
     * Draws 3D space text with full quality (8 shadow layers, 12 glow layers).
     * Use for main menu or non-performance-critical areas.
     * 
     * @param g    graphics context
     * @param text text to render
     * @param font font to use
     * @param x    x position
     * @param y    y position
     */
    public static void draw3DSpaceTextFullQuality(Graphics2D g, String text, Font font, int x, int y) {
        draw3DSpaceText(g, text, font, x, y, 8, 12);
    }
}
