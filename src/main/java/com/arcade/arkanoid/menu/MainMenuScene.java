package com.arcade.arkanoid.menu;

import com.arcade.arkanoid.ArkanoidGame;
import com.arcade.arkanoid.engine.core.GameContext;
import com.arcade.arkanoid.engine.input.InputManager;
import com.arcade.arkanoid.engine.scene.Scene;
import com.arcade.arkanoid.engine.util.FontLoader;
import com.arcade.arkanoid.gameplay.GameplayScene;

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
import javax.imageio.ImageIO;

public class MainMenuScene extends Scene {
    private final Font titleFont = new Font("iomanoid", Font.PLAIN, 150);
    private final Font subtitleFont = new Font("iomanoid", Font.PLAIN, 80);
    private final Font optionFont = new Font("emulogic", Font.PLAIN, 26);
    private final Font hintFont = new Font("Orbitron", Font.PLAIN, 16);
    private String[] options = new String[0];
    private int selectedIndex = 0;
    private BufferedImage backgroundImage;

    public MainMenuScene(GameContext context) {
        super(context);
    }

    @Override
    public void onEnter() {
        FontLoader.loadAll();
        // Load background image from resources (try both JPG and JPEG case variants)
        backgroundImage = null;
        try {
            InputStream is = getClass().getResourceAsStream("/graphics/background.JPG");
            if (is == null) {
                is = getClass().getResourceAsStream("/graphics/background.jpg");
            }
            if (is != null) {
                try (InputStream toRead = is) {
                    backgroundImage = ImageIO.read(toRead);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load background image: " + e.getMessage());
        }
        GameplayScene gameplay = (GameplayScene) context.getScenes().getPersistentScene(ArkanoidGame.SCENE_GAMEPLAY);
        boolean resumeAvailable = gameplay != null && gameplay.isSessionActive();
        options = resumeAvailable
                ? new String[]{"Start New Game", "Resume Game", "Exit"}
                : new String[]{"Start Game", "Exit"};
        selectedIndex = 0;
    }

    @Override
    public void update(double deltaTime) {
        InputManager input = context.getInput();

        if (input.isKeyJustPressed(KeyEvent.VK_UP) || input.isKeyJustPressed(KeyEvent.VK_W)) {
            selectedIndex = (selectedIndex - 1 + options.length) % options.length;
        }
        if (input.isKeyJustPressed(KeyEvent.VK_DOWN) || input.isKeyJustPressed(KeyEvent.VK_S)) {
            selectedIndex = (selectedIndex + 1) % options.length;
        }
        if (input.isKeyJustPressed(KeyEvent.VK_ENTER) || input.isKeyJustPressed(KeyEvent.VK_SPACE)) {
            handleSelection();
        }
    }

    private void handleSelection() {
        String choice = options[selectedIndex];
        GameplayScene gameplay = (GameplayScene) context.getScenes().getPersistentScene(ArkanoidGame.SCENE_GAMEPLAY);
        if ("Start New Game".equals(choice) || "Start Game".equals(choice)) {
            if (gameplay != null) {
                gameplay.beginNewSession();
            }
            context.getScenes().switchTo(ArkanoidGame.SCENE_GAMEPLAY);
        } else if ("Resume Game".equals(choice)) {
            if (gameplay != null && gameplay.isSessionActive()) {
                context.getScenes().switchTo(ArkanoidGame.SCENE_GAMEPLAY);
            }
        } else if ("Exit".equals(choice)) {
            context.getGame().stop();
            System.exit(0);
        }
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
            String option = options[i];
            int optionWidth = graphics.getFontMetrics().stringWidth(option);
            int x = (width - optionWidth) / 2;
            int y = 400 + i * 70;  
            if (i == selectedIndex) {
                graphics.setColor(new Color(0xFFEB3B));
                graphics.drawString("?", x - 40, y);
            }
            graphics.setColor(i == selectedIndex ? Color.WHITE : new Color(220, 220, 220));
            graphics.drawString(option, x, y);
        }

        graphics.setFont(hintFont);
        graphics.setColor(new Color(190, 190, 190));
        graphics.drawString("Use arrow keys or WASD to navigate. Enter to select.", 40, context.getConfig().height() - 60);
        graphics.drawString("Press ESC anytime during the game to pause.", 40, context.getConfig().height() - 30);
    }

    private void drawBackground(Graphics2D graphics) {
        int width = context.getConfig().width();
        int height = context.getConfig().height();

        if (backgroundImage != null) {
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

        GradientPaint spaceGradient = new GradientPaint(
            0, 0, new Color(20, 0, 40), 
            0, height, new Color(0, 0, 10)
        );
        graphics.setPaint(spaceGradient);
        graphics.fillRect(0, 0, width, height);
    }
}
