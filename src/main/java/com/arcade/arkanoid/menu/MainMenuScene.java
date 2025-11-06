package com.arcade.arkanoid.menu;

import com.arcade.arkanoid.ArkanoidGame;
import com.arcade.arkanoid.engine.core.GameContext;
import com.arcade.arkanoid.engine.input.InputManager;
import com.arcade.arkanoid.engine.scene.Scene;
import com.arcade.arkanoid.engine.util.FontLoader;
import com.arcade.arkanoid.engine.assets.AssetManager;
import com.arcade.arkanoid.gameplay.GameplayScene;
import com.arcade.arkanoid.economy.EconomyService;
import com.arcade.arkanoid.localization.LocalizationService;
import com.arcade.arkanoid.profile.PlayerProfile;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * Main menu scene featuring animated 3D title text and navigation options.
 * Displays game title, menu options, player profile, and background animations.
 */
public class MainMenuScene extends Scene {
    /**
     * Enumeration of available menu actions.
     */
    private enum MenuAction {
        WORLD_MAP,
        SHOP,
        RESUME,
        SAVE_SLOTS,
        EXIT
    }

    private final Font titleFont = new Font("iomanoid", Font.PLAIN, 150);
    private final Font subtitleFont = new Font("iomanoid", Font.PLAIN, 80);
    private final Font optionFont = new Font("emulogic", Font.PLAIN, 26);
    private final Font hintFont = new Font("Orbitron", Font.PLAIN, 16);
    private final Font iconLabelFont = new Font("emulogic", Font.PLAIN, 13);

    // Icon layout constants
    private static final int ICON_SIZE = 80;
    private static final int ICON_MARGIN = 40;
    private static final int ICON_LABEL_OFFSET = 25;

    private final LocalizationService localization;
    private final EconomyService economy;
    private MenuAction[] options = new MenuAction[0];
    private int selectedIndex = 0;
    private BufferedImage backgroundImage;
    private BufferedImage backgroundNoPlanets;
    private BufferedImage profileIcon;
    private BufferedImage profilePicture;
    private BufferedImage tutorialIcon;
    private double animationTime = 0;

    /**
     * Constructs a new MainMenuScene.
     * 
     * @param context the game context
     */
    public MainMenuScene(GameContext context) {
        super(context);
        this.localization = context.getLocalizationService();
        this.economy = context.getEconomyService();
    }

    @Override
    public void onEnter() {
        FontLoader.loadAll();
        AssetManager assets = context.getAssets();

        assets.loadImage("background", "/graphics/background.jpg");
        assets.loadImage("background1", "/graphics/background1.jpg");
        assets.loadImage("profile_icon", "/graphics/profile_icon.PNG");
        assets.loadImage("profile_pic", "/graphics/profile_pic.PNG");
        assets.loadImage("tutorial_icon", "/graphics/tutorial_icon.PNG");
        backgroundImage = assets.getImage("background");
        backgroundNoPlanets = assets.getImage("background1");
        profileIcon = assets.getImage("profile_icon");
        profilePicture = assets.getImage("profile_pic");
        tutorialIcon = assets.getImage("tutorial_icon");
        economy.claimDailyBonus();

        GameplayScene gameplay = (GameplayScene) context.getScenes().getPersistentScene(ArkanoidGame.SCENE_GAMEPLAY);
        boolean resumeAvailable = gameplay != null && gameplay.isSessionActive();
        options = resumeAvailable
                ? new MenuAction[] { MenuAction.WORLD_MAP, MenuAction.SHOP, MenuAction.RESUME, MenuAction.SAVE_SLOTS,
                        MenuAction.EXIT }
                : new MenuAction[] { MenuAction.WORLD_MAP, MenuAction.SHOP, MenuAction.SAVE_SLOTS, MenuAction.EXIT };
        selectedIndex = 0;
    }

    @Override
    public void update(double deltaTime) {
        animationTime += deltaTime;
        InputManager input = context.getInput();

        if (input.isKeyJustPressed(KeyEvent.VK_P)) {
            context.getScenes().switchTo(ArkanoidGame.SCENE_PROFILE);
            return;
        }

        if (input.isKeyJustPressed(KeyEvent.VK_T)) {
            // TODO: Add tutorial scene
            // context.getScenes().switchTo(ArkanoidGame.SCENE_TUTORIAL);
            return;
        }

        if (options.length == 0)
            return;

        int move = input.isKeyJustPressed(KeyEvent.VK_UP) || input.isKeyJustPressed(KeyEvent.VK_W) ? -1
                : input.isKeyJustPressed(KeyEvent.VK_DOWN) || input.isKeyJustPressed(KeyEvent.VK_S) ? 1 : 0;

        if (move != 0) {
            selectedIndex = (selectedIndex + move + options.length) % options.length;
        } else if (input.isKeyJustPressed(KeyEvent.VK_ENTER) || input.isKeyJustPressed(KeyEvent.VK_SPACE)) {
            handleSelection();
        }
    }

    /**
     * Handles user selection of menu option.
     * Switches to appropriate scene or exits game based on selected action.
     */
    private void handleSelection() {
        if (options.length == 0) {
            return;
        }
        MenuAction action = options[selectedIndex];
        GameplayScene gameplay = (GameplayScene) context.getScenes().getPersistentScene(ArkanoidGame.SCENE_GAMEPLAY);
        switch (action) {
            case WORLD_MAP:
                context.getScenes().switchTo(ArkanoidGame.SCENE_MAP);
                break;
            case SHOP:
                context.getScenes().switchTo(ArkanoidGame.SCENE_SHOP);
                break;
            case RESUME:
                if (gameplay != null && gameplay.isSessionActive()) {
                    context.getScenes().switchTo(ArkanoidGame.SCENE_GAMEPLAY);
                }
                break;
            case SAVE_SLOTS:
                context.getScenes().switchTo(ArkanoidGame.SCENE_SAVE);
                break;
            case EXIT:
                System.exit(0);
                break;
            default:
                break;
        }
    }

    /**
     * Returns localized label for menu action.
     * 
     * @param action the menu action
     * @return localized label string
     */
    private String labelFor(MenuAction action) {
        switch (action) {
            case RESUME:
                return localization.translate("menu.resume");
            case WORLD_MAP:
                return localization.translate("menu.worldMap");
            case SHOP:
                return localization.translate("menu.shop");
            case SAVE_SLOTS:
                return localization.translate("menu.saveSlots");
            case EXIT:
                return localization.translate("menu.exit");
            default:
                return action.name();
        }
    }

    /**
     * Draws an icon with label at specified position.
     * 
     * @param g     graphics context
     * @param icon  the icon image to draw
     * @param label the text label below icon
     * @param x     x position of icon (left edge)
     */
    private void drawIconWithLabel(Graphics2D g, BufferedImage icon, String label, int x) {
        if (icon != null) {
            int imgW = icon.getWidth();
            int imgH = icon.getHeight();
            double scale = Math.min(ICON_SIZE / (double) imgW, ICON_SIZE / (double) imgH);
            int drawW = (int) (imgW * scale);
            int drawH = (int) (imgH * scale);
            int drawX = x + (ICON_SIZE - drawW) / 2;
            int drawY = ICON_MARGIN + (ICON_SIZE - drawH) / 2;
            g.drawImage(icon, drawX, drawY, drawW, drawH, null);
        }

        g.setFont(iconLabelFont);
        g.setColor(Color.WHITE);
        int textX = x + (ICON_SIZE - g.getFontMetrics().stringWidth(label)) / 2;
        int textY = ICON_MARGIN + ICON_SIZE + ICON_LABEL_OFFSET;
        g.drawString(label, textX, textY);
    }

    /**
     * Draws player profile picture and name in top-left corner.
     * 
     * @param g graphics context
     */
    private void drawPlayerStats(Graphics2D g) {
        PlayerProfile profile = context.getProfileManager().getActiveProfile();
        drawIconWithLabel(g, profileIcon, profile.getDisplayName(), ICON_MARGIN);
    }

    /**
     * Draws tutorial icon and label in top-right corner.
     * 
     * @param g graphics context
     */
    private void drawTutorialIcon(Graphics2D g) {
        int width = context.getConfig().width();
        drawIconWithLabel(g, tutorialIcon, "TUTORIAL", width - ICON_SIZE - ICON_MARGIN);
    }

    /**
     * Renders text with 3D space effect including shadows, glow, and gradient.
     * Creates futuristic sci-fi styled text with depth and neon effects.
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

        for (int depth = 8; depth > 0; depth--) {
            Shape shadowOutline = textLayout.getOutline(AffineTransform.getTranslateInstance(x + depth * 2, y + depth));
            g.setColor(new Color(0, 0, 0, 30));
            g.fill(shadowOutline);
        }

        Shape mainOutline = textLayout.getOutline(AffineTransform.getTranslateInstance(x, y));
        for (int glow = 12; glow > 0; glow--) {
            g.setColor(new Color(0, 150, 255, 8));
            g.setStroke(new BasicStroke(glow * 2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(mainOutline);
        }

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

        g.setStroke(new BasicStroke(2f));
        g.setColor(new Color(255, 255, 255, 200));
        g.draw(mainOutline);
    }

    @Override
    public void render(Graphics2D graphics) {
        drawBackground(graphics);
        int width = context.getConfig().width();
        drawPlayerStats(graphics);
        drawTutorialIcon(graphics);

        String title = "ARKANOID";
        int titleWidth = graphics.getFontMetrics(titleFont).stringWidth(title);
        int titleX = (width - titleWidth) / 2;
        int titleY = 200;
        draw3DSpaceText(graphics, title, titleFont, titleX, titleY);

        String subtitle = "REBORN";
        graphics.setFont(subtitleFont);
        int subtitleWidth = graphics.getFontMetrics().stringWidth(subtitle);
        int subtitleX = (width - subtitleWidth) / 2;
        int subtitleY = titleY + 80;
        draw3DSpaceText(graphics, subtitle, subtitleFont, subtitleX, subtitleY);
        graphics.setFont(optionFont);
        for (int i = 0; i < options.length; i++) {
            String option = labelFor(options[i]);
            int optionWidth = graphics.getFontMetrics().stringWidth(option);
            int x = (width - optionWidth) / 2;
            int y = 400 + i * 70;
            if (i == selectedIndex) {
                graphics.setColor(new Color(0xFFEB3B));
                graphics.drawString(">", x - 40, y);
            }
            graphics.setColor(i == selectedIndex ? Color.WHITE : new Color(220, 220, 220));
            graphics.drawString(option, x, y);
        }

        graphics.setFont(hintFont);
        graphics.setColor(new Color(190, 190, 190));
        graphics.drawString(localization.translate("menu.hint.navigate"), 40, context.getConfig().height() - 90);
        graphics.drawString(localization.translate("menu.hint.pause"), 40, context.getConfig().height() - 60);
        graphics.drawString(localization.translate("menu.hint.profile"), 40, context.getConfig().height() - 30);
        graphics.drawString(localization.translate("menu.hint.tutorial"), 40, context.getConfig().height() - 120);
    }

    /**
     * Draws animated space background with pulsing planet overlay.
     * Uses two background images - base layer and animated planets layer.
     * 
     * @param g graphics context
     */
    private void drawBackground(Graphics2D g) {
        int w = context.getConfig().width(), h = context.getConfig().height();
        BufferedImage bg = backgroundNoPlanets != null ? backgroundNoPlanets : backgroundImage;

        if (bg != null && bg.getWidth() > 0 && bg.getHeight() > 0) {
            double scale = Math.max(w / (double) bg.getWidth(), h / (double) bg.getHeight());
            int drawW = (int) Math.ceil(bg.getWidth() * scale);
            int drawH = (int) Math.ceil(bg.getHeight() * scale);
            int drawX = (w - drawW) / 2, drawY = (h - drawH) / 2;

            g.drawImage(bg, drawX, drawY, drawW, drawH, null);

            if (backgroundNoPlanets != null && backgroundImage != null) {
                float opacity = (float) (0.3 + 0.7 * Math.abs(Math.sin(animationTime * 0.7)));
                g.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, opacity));
                g.drawImage(backgroundImage, drawX, drawY, drawW, drawH, null);
                g.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 1.0f));
            }
        } else {
            g.setPaint(new GradientPaint(0, 0, new Color(20, 0, 40), 0, h, new Color(0, 0, 10)));
            g.fillRect(0, 0, w, h);
        }
    }
}
