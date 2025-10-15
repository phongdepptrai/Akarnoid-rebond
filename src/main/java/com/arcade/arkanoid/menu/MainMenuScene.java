package com.arcade.arkanoid.menu;

import com.arcade.arkanoid.ArkanoidGame;
import com.arcade.arkanoid.engine.core.GameContext;
import com.arcade.arkanoid.engine.input.InputManager;
import com.arcade.arkanoid.engine.scene.Scene;
import com.arcade.arkanoid.engine.util.FontLoader;
import com.arcade.arkanoid.engine.assets.AssetManager;
import com.arcade.arkanoid.gameplay.GameplayScene;
import com.arcade.arkanoid.economy.DailyBonusResult;
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
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import javax.imageio.ImageIO;

public class MainMenuScene extends Scene {
    private enum MenuAction {
        START_NEW,
        START,
        RESUME,
        WORLD_MAP,
        EXIT
    }

    private final Font titleFont = new Font("iomanoid", Font.PLAIN, 150);
    private final Font subtitleFont = new Font("iomanoid", Font.PLAIN, 80);
    private final Font optionFont = new Font("emulogic", Font.PLAIN, 26);
    private final Font hintFont = new Font("Orbitron", Font.PLAIN, 16);
    private final LocalizationService localization;
    private final EconomyService economy;
    private MenuAction[] options = new MenuAction[0];
    private int selectedIndex = 0;
    private BufferedImage backgroundImage;
    private BufferedImage backgroundNoPlanets;
    private DailyBonusResult dailyBonusResult;
    private String dailyBonusMessage = "";
    private double animationTime = 0;

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
        backgroundImage = assets.getImage("background");
        backgroundNoPlanets = assets.getImage("background1");

        dailyBonusResult = economy.claimDailyBonus();
        dailyBonusMessage = formatDailyBonusMessage(dailyBonusResult);

        GameplayScene gameplay = (GameplayScene) context.getScenes().getPersistentScene(ArkanoidGame.SCENE_GAMEPLAY);
        boolean resumeAvailable = gameplay != null && gameplay.isSessionActive();
        options = resumeAvailable
                ? new MenuAction[]{MenuAction.START_NEW, MenuAction.RESUME, MenuAction.WORLD_MAP, MenuAction.EXIT}
                : new MenuAction[]{MenuAction.START, MenuAction.WORLD_MAP, MenuAction.EXIT};
        selectedIndex = 0;
    }

    @Override
    public void update(double deltaTime) {
        animationTime += deltaTime;
        InputManager input = context.getInput();

        if (options.length > 0) {
            int move = 0;
            if (input.isKeyJustPressed(KeyEvent.VK_UP) || input.isKeyJustPressed(KeyEvent.VK_W)) {
                move = -1;
            }
            if (input.isKeyJustPressed(KeyEvent.VK_DOWN) || input.isKeyJustPressed(KeyEvent.VK_S)) {
                if (move == 0) {
                    move = 1;
                }
            }
            if (move != 0) {
                selectedIndex = Math.max(0, Math.min(selectedIndex + move, options.length - 1));
            }
            if (move == 0 && (input.isKeyJustPressed(KeyEvent.VK_ENTER) || input.isKeyJustPressed(KeyEvent.VK_SPACE))) {
                handleSelection();
            }
        }
    }

    private void handleSelection() {
        if (options.length == 0) {
            return;
        }
        MenuAction action = options[selectedIndex];
        GameplayScene gameplay = (GameplayScene) context.getScenes().getPersistentScene(ArkanoidGame.SCENE_GAMEPLAY);
        switch (action) {
            case START_NEW:
            case START:
                context.getProfileManager().getActiveProfile().setCurrentLevelId("001");
                context.getProfileManager().saveProfile();
                if (gameplay != null) {
                    gameplay.beginNewSession();
                }
                context.getScenes().switchTo(ArkanoidGame.SCENE_GAMEPLAY);
                break;
            case RESUME:
                if (gameplay != null && gameplay.isSessionActive()) {
                    context.getScenes().switchTo(ArkanoidGame.SCENE_GAMEPLAY);
                }
                break;
            case WORLD_MAP:
                context.getScenes().switchTo(ArkanoidGame.SCENE_MAP);
                break;
            case EXIT:
                context.getGame().stop();
                System.exit(0);
                break;
            default:
                break;
        }
    }

    private String labelFor(MenuAction action) {
        switch (action) {
            case START_NEW:
                return localization.translate("menu.startNew");
            case START:
                return localization.translate("menu.start");
            case RESUME:
                return localization.translate("menu.resume");
            case WORLD_MAP:
                return localization.translate("menu.worldMap");
            case EXIT:
                return localization.translate("menu.exit");
            default:
                return action.name();
        }
    }

    private void drawPlayerStats(Graphics2D graphics) {
        PlayerProfile profile = context.getProfileManager().getActiveProfile();
        graphics.setFont(hintFont);
        graphics.setColor(new Color(210, 210, 210));
        int y = 40;
        graphics.drawString(localization.translate("menu.status.lives", profile.getLives(), profile.getMaxLives()), 40, y);
        y += 24;
        graphics.drawString(localization.translate("menu.status.coins", profile.getCoins()), 40, y);
        y += 24;
        graphics.drawString(localization.translate("menu.status.energy", profile.getEnergy(), profile.getMaxEnergy()), 40, y);
        y += 24;
        graphics.drawString(localization.translate("menu.status.streak", profile.getDailyStreak()), 40, y);
        y += 30;
        if (dailyBonusMessage != null && !dailyBonusMessage.isBlank()) {
            graphics.drawString(dailyBonusMessage, 40, y);
            y += 22;
        }
        String nextBonus = formatNextBonusCountdown(profile);
        if (!nextBonus.isBlank()) {
            graphics.drawString(nextBonus, 40, y);
        }
    }

    private String formatDailyBonusMessage(DailyBonusResult result) {
        if (result == null) {
            return "";
        }
        if (result.isGranted()) {
            return localization.translate("menu.dailyBonus.claimed",
                    result.getCoinsAwarded(),
                    result.getLivesAwarded(),
                    result.getStreak());
        }
        return localization.translate("menu.dailyBonus.already", result.getStreak());
    }

    private String formatNextBonusCountdown(PlayerProfile profile) {
        long lastClaim = profile.getLastDailyBonusEpochSeconds();
        if (lastClaim <= 0) {
            return "";
        }
        Instant now = Instant.now();
        LocalDate lastClaimDate = Instant.ofEpochSecond(lastClaim).atZone(ZoneOffset.UTC).toLocalDate();
        LocalDate nextClaimDate = lastClaimDate.plusDays(1);
        Instant nextClaimInstant = nextClaimDate.atStartOfDay().toInstant(ZoneOffset.UTC);
        if (!now.isBefore(nextClaimInstant)) {
            return localization.translate("menu.dailyBonus.ready");
        }
        long seconds = Math.max(0, Duration.between(now, nextClaimInstant).getSeconds());
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        String formatted = String.format("%02d:%02d:%02d", hours, minutes, secs);
        return localization.translate("menu.dailyBonus.next", formatted);
    }

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
            new float[]{0f, 0.3f, 0.6f, 1f},
            new Color[]{
                new Color(0x00FFFF), 
                new Color(0x0099FF), 
                new Color(0x6633FF), 
                new Color(0x9900CC)  
            }
        );
        
        g.setPaint(spaceGradient);
        g.fill(mainOutline);
        
        g.setStroke(new BasicStroke(2f));
        g.setColor(new Color(255, 255, 255, 200));
        g.draw(mainOutline);
        
        LinearGradientPaint highlight = new LinearGradientPaint(
            x, y - 60, x, y - 40,
            new float[]{0f, 1f},
            new Color[]{new Color(255, 255, 255, 100), new Color(255, 255, 255, 0)}
        );
    
    }


    @Override
    public void render(Graphics2D graphics) {
        drawBackground(graphics);
        int width = context.getConfig().width();
        drawPlayerStats(graphics);
        
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
        graphics.drawString(localization.translate("menu.hint.navigate"), 40, context.getConfig().height() - 60);
        graphics.drawString(localization.translate("menu.hint.pause"), 40, context.getConfig().height() - 30);
    }

    private void drawBackground(Graphics2D graphics) {
        int width = context.getConfig().width();
        int height = context.getConfig().height();

        if (backgroundNoPlanets != null) {
            // Always draw base background without planets
            int imgW = backgroundNoPlanets.getWidth();
            int imgH = backgroundNoPlanets.getHeight();
            if (imgW > 0 && imgH > 0) {
                double scale = Math.max(width / (double) imgW, height / (double) imgH);
                int drawW = (int) Math.ceil(imgW * scale);
                int drawH = (int) Math.ceil(imgH * scale);
                int drawX = (width - drawW) / 2;
                int drawY = (height - drawH) / 2;
                graphics.drawImage(backgroundNoPlanets, drawX, drawY, drawW, drawH, null);
                
                // Draw planets with pulsing opacity
                if (backgroundImage != null) {
                    // Create pulsing effect (slow fade in/out cycle)
                    float planetOpacity = (float)(0.3 + 0.7 * Math.abs(Math.sin(animationTime * 0.4)));
                    
                    java.awt.AlphaComposite alphaComposite = java.awt.AlphaComposite.getInstance(
                        java.awt.AlphaComposite.SRC_OVER, planetOpacity
                    );
                    graphics.setComposite(alphaComposite);
                    graphics.drawImage(backgroundImage, drawX, drawY, drawW, drawH, null);
                    
                    // Reset composite
                    graphics.setComposite(java.awt.AlphaComposite.getInstance(
                        java.awt.AlphaComposite.SRC_OVER, 1.0f
                    ));
                }
                return;
            }
        } else if (backgroundImage != null) {
            // Fallback to single background if background1 not available
            int imgW = backgroundImage.getWidth();
            int imgH = backgroundImage.getHeight();
            if (imgW > 0 && imgH > 0) {
                double scale = Math.max(width / (double) imgW, height / (double) imgH);
                int drawW = (int) Math.ceil(imgW * scale);
                int drawH = (int) Math.ceil(imgH * scale);
                int drawX = (width - drawW) / 2;
                int drawY = (height - drawH) / 2;
                graphics.drawImage(backgroundImage, drawX, drawY, drawW, drawH, null);
                return;
            }
        }

        // Fallback gradient if no images available
        GradientPaint spaceGradient = new GradientPaint(
            0, 0, new Color(20, 0, 40), 
            0, height, new Color(0, 0, 10)
        );
        graphics.setPaint(spaceGradient);
        graphics.fillRect(0, 0, width, height);
    }
}
