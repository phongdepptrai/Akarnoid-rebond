package com.arcade.arkanoid.menu;

import com.arcade.arkanoid.ArkanoidGame;
import com.arcade.arkanoid.engine.core.GameContext;
import com.arcade.arkanoid.engine.input.InputManager;
import com.arcade.arkanoid.engine.scene.Scene;
import com.arcade.arkanoid.engine.util.FontLoader;
import com.arcade.arkanoid.gameplay.GameplayScene;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

public class MainMenuScene extends Scene {
    private final Font titleFont = new Font("optimus", Font.BOLD, 56);
    private final Font optionFont = new Font("emulogic", Font.BOLD, 26);
    private final Font hintFont = new Font("generation", Font.PLAIN, 16);
    private String[] options = new String[0];
    private int selectedIndex = 0;

    public MainMenuScene(GameContext context) {
        super(context);
    }

    @Override
    public void onEnter() {
        FontLoader.loadAll();
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

    @Override
    public void render(Graphics2D graphics) {
        drawBackground(graphics);
        int width = context.getConfig().width();
        graphics.setFont(titleFont);
        graphics.setColor(Color.WHITE);
        String title = "Arkanoid Reborn";
        int titleWidth = graphics.getFontMetrics().stringWidth(title);
        graphics.drawString(title, (width - titleWidth) / 2, 180);

        graphics.setFont(optionFont);
        for (int i = 0; i < options.length; i++) {
            String option = options[i];
            int optionWidth = graphics.getFontMetrics().stringWidth(option);
            int x = (width - optionWidth) / 2;
            int y = 280 + i * 60;
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
        GradientPaint gradient = new GradientPaint(0, 0, new Color(17, 32, 64), 0, height, new Color(5, 8, 13));
        graphics.setPaint(gradient);
        graphics.fillRect(0, 0, width, height);
    }
}
