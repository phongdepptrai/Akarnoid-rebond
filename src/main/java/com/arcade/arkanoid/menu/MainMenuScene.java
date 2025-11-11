package com.arcade.arkanoid.menu;

import com.arcade.arkanoid.ArkanoidGame;
import com.arcade.arkanoid.engine.core.GameContext;
import com.arcade.arkanoid.engine.input.InputManager;
import com.arcade.arkanoid.engine.scene.Scene;
import com.arcade.arkanoid.engine.util.FontLoader;
import com.arcade.arkanoid.engine.util.GradientUtils;
import com.arcade.arkanoid.engine.assets.AssetManager;
import com.arcade.arkanoid.engine.audio.BackgroundMusicManager;
import com.arcade.arkanoid.gameplay.GameplayScene;
import com.arcade.arkanoid.economy.EconomyService;
import com.arcade.arkanoid.localization.LocalizationService;
import com.arcade.arkanoid.engine.settings.SettingsManager;
import com.arcade.arkanoid.profile.PlayerProfile;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
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
        SETTINGS,
        EXIT
    }

    private final Font titleFont = new Font("iomanoid", Font.PLAIN, 150);
    private final Font subtitleFont = new Font("iomanoid", Font.PLAIN, 80);
    private final Font optionFont = new Font("BoldPixels", Font.PLAIN, 46);
    private final Font hintFont = new Font("BoldPixels", Font.PLAIN, 16);
    private final Font iconLabelFont = new Font("BoldPixels", Font.PLAIN, 38);

    // Icon layout constants
    private static final int ICON_SIZE = 80;
    private static final int ICON_MARGIN = 40;
    private static final int ICON_LABEL_OFFSET = 25;

    private final LocalizationService localization;
    private final EconomyService economy;
    private final SettingsManager settings;
    private MenuAction[] options = new MenuAction[0];
    private int selectedIndex = 0;
    private BufferedImage backgroundImage;
    private BufferedImage backgroundNoPlanets;
    private BufferedImage profileIcon;
    private BufferedImage tutorialIcon;
    private double animationTime = 0;

    // Cached title rendering for performance
    private BufferedImage cachedTitle;
    private BufferedImage cachedSubtitle;

    /**
     * Constructs a new MainMenuScene.
     * 
     * @param context the game context
     */
    public MainMenuScene(GameContext context) {
        super(context);
        this.localization = context.getLocalizationService();
        this.economy = context.getEconomyService();
        this.settings = context.getSettingsManager();
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
        tutorialIcon = assets.getImage("tutorial_icon");
        economy.claimDailyBonus();

        BackgroundMusicManager musicManager = BackgroundMusicManager.getInstance();
        musicManager.setVolume(settings.getMusicVolume() / 100f);
        musicManager.playTheme("menu_theme", "/sounds/theme_song.mp3");
        createCachedTitles();

        GameplayScene gameplay = (GameplayScene) context.getScenes().getPersistentScene(ArkanoidGame.SCENE_GAMEPLAY);
        boolean resumeAvailable = gameplay != null && gameplay.isSessionActive();
        options = resumeAvailable
                ? new MenuAction[] { MenuAction.WORLD_MAP, MenuAction.SHOP, MenuAction.RESUME, MenuAction.SAVE_SLOTS,
                        MenuAction.SETTINGS, MenuAction.EXIT }
                : new MenuAction[] { MenuAction.WORLD_MAP, MenuAction.SHOP, MenuAction.SAVE_SLOTS,
                        MenuAction.SETTINGS, MenuAction.EXIT };
        selectedIndex = 0;
    }

    @Override
    public void onExit() {
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
                    BackgroundMusicManager.getInstance().stopTheme();
                    context.getScenes().switchTo(ArkanoidGame.SCENE_GAMEPLAY);
                }
                break;
            case SAVE_SLOTS:
                context.getScenes().switchTo(ArkanoidGame.SCENE_SAVE);
                break;
            case SETTINGS:
                context.getScenes().switchTo(ArkanoidGame.SCENE_SETTINGS);
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
            case SETTINGS:
                return localization.translate("menu.settings");
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
     * Creates cached BufferedImages for title and subtitle to improve performance.
     * These are rendered once at high quality and reused every frame.
     */
    private void createCachedTitles() {
        // Cache title
        String title = "ARKANOID";
        int titleWidth = (int) titleFont.createGlyphVector(
                new java.awt.font.FontRenderContext(null, true, true), title).getVisualBounds().getWidth();
        int titleHeight = 200;

        cachedTitle = new BufferedImage(titleWidth + 100, titleHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = cachedTitle.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        GradientUtils.draw3DSpaceTextFullQuality(g, title, titleFont, 50, 150);
        g.dispose();

        // Cache subtitle
        String subtitle = "REBORN";
        int subtitleWidth = (int) subtitleFont.createGlyphVector(
                new java.awt.font.FontRenderContext(null, true, true), subtitle).getVisualBounds().getWidth();
        int subtitleHeight = 150;

        cachedSubtitle = new BufferedImage(subtitleWidth + 100, subtitleHeight, BufferedImage.TYPE_INT_ARGB);
        g = cachedSubtitle.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        GradientUtils.draw3DSpaceTextFullQuality(g, subtitle, subtitleFont, 50, 100);
        g.dispose();
    }

    @Override
    public void render(Graphics2D graphics) {
        drawBackground(graphics);
        int width = context.getConfig().width();
        drawPlayerStats(graphics);
        drawTutorialIcon(graphics);

        // Draw cached title
        if (cachedTitle != null) {
            String title = "ARKANOID";
            int titleWidth = graphics.getFontMetrics(titleFont).stringWidth(title);
            int titleX = (width - titleWidth) / 2 - 50;
            int titleY = 50;
            graphics.drawImage(cachedTitle, titleX, titleY, null);
        }

        // Draw cached subtitle
        if (cachedSubtitle != null) {
            String subtitle = "REBORN";
            graphics.setFont(subtitleFont);
            int subtitleWidth = graphics.getFontMetrics().stringWidth(subtitle);
            int subtitleX = (width - subtitleWidth) / 2 - 50;
            int subtitleY = 200;
            graphics.drawImage(cachedSubtitle, subtitleX, subtitleY, null);
        }

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
        graphics.drawString(localization.translate("menu.hint.profile"), 40, context.getConfig().height() - 30);
        graphics.drawString(localization.translate("menu.hint.tutorial"), 40, context.getConfig().height() - 60);
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
