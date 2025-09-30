package com.arcade.arkanoid.gameplay.system;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

public class HudRenderer {
    private final Font labelFont = new Font("SansSerif", Font.BOLD, 20);
    private final Font messageFont = new Font("SansSerif", Font.BOLD, 32);

    public void renderHud(Graphics2D graphics, int score, int lives, String levelName) {
        graphics.setFont(labelFont);
        graphics.setColor(Color.WHITE);
        graphics.drawString("Score: " + score, 20, 30);
        graphics.drawString("Lives: " + lives, 180, 30);
        graphics.drawString("Level: " + levelName, 320, 30);
    }

    public void renderCenterMessage(Graphics2D graphics, String message, int canvasWidth, int canvasHeight) {
        graphics.setFont(messageFont);
        graphics.setColor(Color.WHITE);
        int textWidth = graphics.getFontMetrics().stringWidth(message);
        int x = (canvasWidth - textWidth) / 2;
        int y = canvasHeight / 2;
        graphics.drawString(message, x, y);
    }
}
