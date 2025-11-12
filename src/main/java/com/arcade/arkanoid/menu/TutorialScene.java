package com.arcade.arkanoid.menu;

import com.arcade.arkanoid.ArkanoidGame;
import com.arcade.arkanoid.engine.core.GameContext;
import com.arcade.arkanoid.engine.input.InputManager;
import com.arcade.arkanoid.engine.scene.Scene;
import com.arcade.arkanoid.gameplay.entities.PowerUp;
import com.arcade.arkanoid.localization.LocalizationService;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;

/**
 * Tutorial scene to teach players about game controls and power-ups.
 */
public class TutorialScene extends Scene {
    private static final int PAGE_CONTROLS = 0;
    private static final int PAGE_POWERUPS = 1;
    private static final int TOTAL_PAGES = 2;

    private final Font titleFont = new Font("BoldPixels", Font.BOLD, 56);
    private final Font subtitleFont = new Font("BoldPixels", Font.BOLD, 32);
    private final Font textFont = new Font("BoldPixels", Font.PLAIN, 24);
    private final Font hintFont = new Font("BoldPixels", Font.PLAIN, 18);
    private final Font powerupFont = new Font("BoldPixels", Font.PLAIN, 22);

    private final LocalizationService localization;
    private int currentPage = 0;

    public TutorialScene(GameContext context) {
        super(context);
        this.localization = context.getLocalizationService();
    }

    @Override
    public void onEnter() {
        currentPage = 0;
    }

    @Override
    public void update(double deltaTime) {
        InputManager input = context.getInput();

        if (input.isKeyJustPressed(KeyEvent.VK_ESCAPE)) {
            context.getScenes().switchTo(ArkanoidGame.SCENE_MENU);
            return;
        }

        if (input.isKeyJustPressed(KeyEvent.VK_LEFT) || input.isKeyJustPressed(KeyEvent.VK_A)) {
            currentPage = (currentPage - 1 + TOTAL_PAGES) % TOTAL_PAGES;
        }

        if (input.isKeyJustPressed(KeyEvent.VK_RIGHT) || input.isKeyJustPressed(KeyEvent.VK_D)) {
            currentPage = (currentPage + 1) % TOTAL_PAGES;
        }

        if (input.isKeyJustPressed(KeyEvent.VK_ENTER) || input.isKeyJustPressed(KeyEvent.VK_SPACE)) {
            context.getScenes().switchTo(ArkanoidGame.SCENE_MENU);
        }
    }

    @Override
    public void render(Graphics2D graphics) {
        int width = context.getConfig().width();
        int height = context.getConfig().height();

        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        graphics.setColor(new Color(12, 16, 40));
        graphics.fillRect(0, 0, width, height);

        if (currentPage == PAGE_CONTROLS) {
            renderControlsPage(graphics, width, height);
        } else if (currentPage == PAGE_POWERUPS) {
            renderPowerupsPage(graphics, width, height);
        }

        renderNavigation(graphics, width, height);
    }

    private void renderControlsPage(Graphics2D graphics, int width, int height) {
        graphics.setFont(titleFont);
        graphics.setColor(new Color(255, 215, 0));
        String title = localization.translate("tutorial.title.controls");
        int titleWidth = graphics.getFontMetrics().stringWidth(title);
        graphics.drawString(title, (width - titleWidth) / 2, 80);

        graphics.setFont(textFont);
        graphics.setColor(Color.WHITE);

        int y = 160;
        int lineHeight = 50;

        String[] controls = {
                localization.translate("tutorial.control.moveLeft"),
                localization.translate("tutorial.control.moveRight"),
                localization.translate("tutorial.control.launch"),
                localization.translate("tutorial.control.pause")
        };

        for (String line : controls) {
            int lineWidth = graphics.getFontMetrics().stringWidth(line);
            graphics.drawString(line, (width - lineWidth) / 2, y);
            y += lineHeight;
        }

        y += 30;

        graphics.setFont(subtitleFont);
        graphics.setColor(new Color(100, 200, 255));
        String objectivesTitle = localization.translate("tutorial.objectives.title");
        int objTitleWidth = graphics.getFontMetrics().stringWidth(objectivesTitle);
        graphics.drawString(objectivesTitle, (width - objTitleWidth) / 2, y);

        y += 50;

        graphics.setFont(textFont);
        graphics.setColor(Color.WHITE);

        String[] objectives = {
                localization.translate("tutorial.objectives.destroy"),
                localization.translate("tutorial.objectives.keepBall"),
                localization.translate("tutorial.objectives.complete")
        };

        for (String line : objectives) {
            int lineWidth = graphics.getFontMetrics().stringWidth(line);
            graphics.drawString(line, (width - lineWidth) / 2, y);
            y += lineHeight;
        }
    }

    private void renderPowerupsPage(Graphics2D graphics, int width, int height) {
        graphics.setFont(titleFont);
        graphics.setColor(new Color(255, 215, 0));
        String title = localization.translate("tutorial.title.powerups");
        int titleWidth = graphics.getFontMetrics().stringWidth(title);
        graphics.drawString(title, (width - titleWidth) / 2, 80);

        graphics.setFont(powerupFont);

        int startY = 160;
        int spacing = 68;
        int powerupSize = 36;
        int iconX = width / 2 - 250;
        int textX = iconX + powerupSize + 20;

        PowerUp.Type[] powerups = {
                PowerUp.Type.EXPAND_PADDLE,
                PowerUp.Type.SLOW_BALL,
                PowerUp.Type.MULTI_BALL,
                PowerUp.Type.EXTRA_LIFE,
                PowerUp.Type.FIRE_BALL,
                PowerUp.Type.PADDLE_GUN
        };

        String[] descriptions = {
                localization.translate("tutorial.powerup.expand"),
                localization.translate("tutorial.powerup.slow"),
                localization.translate("tutorial.powerup.multi"),
                localization.translate("tutorial.powerup.life"),
                localization.translate("tutorial.powerup.fire"),
                localization.translate("tutorial.powerup.gun")
        };

        Color[] colors = {
                new Color(0x00FF00), // EXPAND_PADDLE - green
                new Color(0x00FFFF), // SLOW_BALL - cyan
                new Color(0xFFFF00), // MULTI_BALL - yellow
                new Color(0xFF00FF), // EXTRA_LIFE - magenta
                new Color(0xFF6600), // FIRE_BALL - orange
                new Color(0xFFFFFF) // PADDLE_GUN - white
        };

        for (int i = 0; i < powerups.length; i++) {
            int y = startY + i * spacing;
            int iconY = y - powerupSize / 2;

            graphics.setColor(colors[i]);
            graphics.fillOval(iconX, iconY, powerupSize, powerupSize);

            graphics.setColor(Color.BLACK);
            graphics.setFont(new Font("BoldPixels", Font.BOLD, 20));
            String symbol = getSymbol(powerups[i]);
            int symbolWidth = graphics.getFontMetrics().stringWidth(symbol);
            int symbolX = iconX + (powerupSize - symbolWidth) / 2;
            int symbolY = iconY + powerupSize / 2 + 7;
            graphics.drawString(symbol, symbolX, symbolY);

            graphics.setColor(Color.WHITE);
            graphics.setFont(powerupFont);
            graphics.drawString(descriptions[i], textX, y);
        }
    }

    private String getSymbol(PowerUp.Type type) {
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

    private void renderNavigation(Graphics2D graphics, int width, int height) {
        graphics.setFont(hintFont);
        graphics.setColor(new Color(150, 150, 150));

        String pageInfo = localization.translate("tutorial.navigation.page",
                String.valueOf(currentPage + 1), String.valueOf(TOTAL_PAGES));
        int pageWidth = graphics.getFontMetrics().stringWidth(pageInfo);
        graphics.drawString(pageInfo, (width - pageWidth) / 2, height - 70);

        String nav = localization.translate("tutorial.navigation.hint");
        int navWidth = graphics.getFontMetrics().stringWidth(nav);
        graphics.drawString(nav, (width - navWidth) / 2, height - 35);
    }
}
