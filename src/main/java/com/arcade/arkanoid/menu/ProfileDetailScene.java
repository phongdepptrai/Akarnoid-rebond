package com.arcade.arkanoid.menu;

import com.arcade.arkanoid.ArkanoidGame;
import com.arcade.arkanoid.engine.core.GameContext;
import com.arcade.arkanoid.engine.scene.Scene;
import com.arcade.arkanoid.engine.input.InputManager;
import com.arcade.arkanoid.engine.audio.BackgroundMusicManager;
import com.arcade.arkanoid.profile.PlayerProfile;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

/**
 * Scene displaying detailed player profile information with statistics.
 * Features a sci-fi themed UI with animated background and cyan borders.
 */
public class ProfileDetailScene extends Scene {
    private static final Font TITLE_FONT = new Font("BoldPixels", Font.BOLD, 60);
    private static final Font LABEL_FONT = new Font("BoldPixels", Font.PLAIN, 30);
    private static final Font VALUE_FONT = new Font("BoldPixels", Font.PLAIN, 26);
    private static final Font HINT_FONT = new Font("BoldPixels", Font.PLAIN, 26);
    private static final Color CYAN_LABEL = new Color(100, 180, 255);

    private BufferedImage profilePicture;
    private BufferedImage backgroundImage;
    private BufferedImage backgroundNoPlanets;
    private double animationTime = 0;

    /**
     * Constructs a new ProfileDetailScene.
     * 
     * @param context the game context
     */
    public ProfileDetailScene(GameContext context) {
        super(context);
    }

    @Override
    public void onEnter() {
        // Load profile picture and backgrounds
        profilePicture = context.getAssets().getImage("profile_pic");
        context.getAssets().loadImage("background", "/graphics/background.jpg");
        context.getAssets().loadImage("background1", "/graphics/background1.jpg");
        backgroundImage = context.getAssets().getImage("background");
        backgroundNoPlanets = context.getAssets().getImage("background1");
        animationTime = 0;

        // Start background music using singleton
        BackgroundMusicManager musicManager = BackgroundMusicManager.getInstance();
        musicManager.setVolume(context.getSettingsManager().getMusicVolume() / 100f);
        musicManager.playTheme("menu_theme", "/sounds/theme_song.mp3");
    }

    @Override
    public void onExit() {
        // Music will continue playing when switching between menu scenes
    }

    @Override
    public void update(double deltaTime) {
        animationTime += deltaTime;
        InputManager input = context.getInput();

        // Press ESC or ENTER to go back
        if (input.isKeyJustPressed(KeyEvent.VK_ESCAPE) ||
                input.isKeyJustPressed(KeyEvent.VK_ENTER)) {
            context.getScenes().switchTo(ArkanoidGame.SCENE_MENU);
        }
    }

    @Override
    public void render(Graphics2D g) {
        int width = context.getConfig().width();
        int height = context.getConfig().height();

        drawBackground(g);
        g.setColor(new Color(10, 10, 20, 200));
        g.fillRect(0, 0, width, height);

        int panelW = 600, panelH = 700;
        int panelX = (width - panelW) / 2, panelY = (height - panelH) / 2;

        drawPanel(g, panelX, panelY, panelW, panelH);

        PlayerProfile profile = context.getProfileManager().getActiveProfile();

        // Title
        drawCenteredText(g, "PLAYER PROFILE", TITLE_FONT, Color.WHITE, panelX + panelW / 2, panelY + 50);

        // Profile picture
        int picSize = 120, picX = panelX + (panelW - picSize) / 2, picY = panelY + 80;
        drawProfilePicture(g, picX, picY, picSize);

        // Display name and ID
        drawCenteredText(g, profile.getDisplayName(), new Font("BoldPixels", Font.BOLD, 30),
                Color.WHITE, panelX + panelW / 2, picY + picSize + 40);
        drawCenteredText(g, "ID: " + profile.getPlayerId().substring(0, 8) + "...",
                new Font("Orbitron", Font.PLAIN, 14), new Color(150, 150, 150),
                panelX + panelW / 2, picY + picSize + 60);

        // Stats
        int statsY = picY + picSize + 100, leftX = panelX + panelW / 4, rightX = panelX + 3 * panelW / 4, rowH = 70;
        drawStat(g, "LIVES", profile.getLives() + " / " + profile.getMaxLives(), leftX, statsY);
        drawStat(g, "COINS", String.valueOf(profile.getCoins()), leftX, statsY + rowH);
        drawStat(g, "ENERGY", profile.getEnergy() + " / " + profile.getMaxEnergy(), leftX, statsY + rowH * 2);
        drawStat(g, "STREAK", profile.getDailyStreak() + " DAYS", rightX, statsY);
        drawStat(g, "UNLOCKED", profile.getUnlockedLevelIds().size() + " LEVELS", rightX, statsY + rowH);
        drawStat(g, "COMPLETED", profile.getCompletedLevelIds().size() + " LEVELS", rightX, statsY + rowH * 2);

        // Daily bonus and hint
        drawCenteredText(g, getDailyBonusStatus(profile), HINT_FONT, new Color(200, 200, 255),
                panelX + panelW / 2, statsY + rowH * 3 + 20);
        drawCenteredText(g, "Press ESC or ENTER to close", HINT_FONT, new Color(150, 150, 150),
                panelX + panelW / 2, panelY + panelH - 30);
    }

    /**
     * Draws centered text at specified position.
     */
    private void drawCenteredText(Graphics2D g, String text, Font font, Color color, int centerX, int y) {
        g.setFont(font);
        g.setColor(color);
        g.drawString(text, centerX - g.getFontMetrics().stringWidth(text) / 2, y);
    }

    /**
     * Draws profile picture with glow effect or fallback placeholder.
     */
    private void drawProfilePicture(Graphics2D g, int x, int y, int size) {
        if (profilePicture != null) {
            g.setColor(new Color(0, 150, 255, 100));
            g.fillOval(x - 10, y - 10, size + 20, size + 20);
            g.drawImage(profilePicture, x, y, size, size, null);
        }
    }

    /**
     * Draws a stat label and value centered at specified position.
     */
    private void drawStat(Graphics2D g, String label, String value, int centerX, int y) {
        g.setFont(LABEL_FONT);
        g.setColor(CYAN_LABEL);
        g.drawString(label, centerX - g.getFontMetrics().stringWidth(label) / 2, y);
        g.setFont(VALUE_FONT);
        g.setColor(Color.WHITE);
        g.drawString(value, centerX - g.getFontMetrics().stringWidth(value) / 2, y + 32);
    }

    /**
     * Returns formatted daily bonus status message.
     */
    private String getDailyBonusStatus(PlayerProfile profile) {
        long lastClaim = profile.getLastDailyBonusEpochSeconds();
        if (lastClaim <= 0)
            return "🎁 Daily Bonus: Available Now!";

        Instant now = Instant.now();
        Instant nextClaim = Instant.ofEpochSecond(lastClaim)
                .atZone(ZoneOffset.UTC).toLocalDate().plusDays(1)
                .atStartOfDay().toInstant(ZoneOffset.UTC);

        if (!now.isBefore(nextClaim))
            return "🎁 Daily Bonus: Ready to claim!";

        long sec = Math.max(0, Duration.between(now, nextClaim).getSeconds());
        return String.format("🎁 Next Bonus in: %02d:%02d:%02d", sec / 3600, (sec % 3600) / 60, sec % 60);
    }

    /**
     * Draws animated space background with pulsing planet overlay.
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

            // Pulsing planets overlay
            if (backgroundNoPlanets != null && backgroundImage != null) {
                float opacity = (float) (0.3 + 0.7 * Math.abs(Math.sin(animationTime * 0.7)));
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
                g.drawImage(backgroundImage, drawX, drawY, drawW, drawH, null);
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            }
        } else {
            // Fallback gradient
            g.setPaint(new GradientPaint(0, 0, new Color(20, 0, 40), 0, h, new Color(0, 0, 10)));
            g.fillRect(0, 0, w, h);
        }
    }

    /**
     * Draws cyan bordered panel with glow effect (no background fill).
     */
    private void drawPanel(Graphics2D g, int x, int y, int w, int h) {
        // Only draw bright cyan border with glow - no background fill
        g.setColor(new Color(0, 150, 255, 100));
        g.setStroke(new BasicStroke(3f));
        g.drawRoundRect(x, y, w, h, 30, 30);
    }
}
