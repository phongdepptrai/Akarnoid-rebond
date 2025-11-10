package com.arcade.arkanoid.menu;

import com.arcade.arkanoid.ArkanoidGame;
import com.arcade.arkanoid.engine.core.GameContext;
import com.arcade.arkanoid.engine.input.InputManager;
import com.arcade.arkanoid.engine.scene.Scene;
import com.arcade.arkanoid.gameplay.GameplayScene;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

public class PauseScene extends Scene {
    private final Font titleFont = new Font("emulogic", Font.PLAIN, 42);
    private final Font optionFont = new Font("emulogic", Font.PLAIN, 20);
    private final String[] options = { "Resume", "Restart Level", "Main Menu" };
    private int selected = 0;
    private GameplayScene gameplayScene;

    public PauseScene(GameContext context) {
        super(context);
    }

    public void bindGameplay(GameplayScene gameplayScene) {
        this.gameplayScene = gameplayScene;
        selected = 0;
    }

    @Override
    public void onEnter() {
        selected = 0;
    }

    @Override
    public void update(double deltaTime) {
        InputManager input = context.getInput();

        if (input.isKeyJustPressed(KeyEvent.VK_UP) || input.isKeyJustPressed(KeyEvent.VK_W)) {
            selected = (selected - 1 + options.length) % options.length;
        }
        if (input.isKeyJustPressed(KeyEvent.VK_DOWN) || input.isKeyJustPressed(KeyEvent.VK_S)) {
            selected = (selected + 1) % options.length;
        }
        if (input.isKeyJustPressed(KeyEvent.VK_ENTER) || input.isKeyJustPressed(KeyEvent.VK_SPACE)) {
            executeSelection();
        }
        if (input.isKeyJustPressed(KeyEvent.VK_ESCAPE)) {
            resumeGameplay();
        }
    }

    private void executeSelection() {
        if (gameplayScene == null) {
            resumeGameplay();
            return;
        }
        String choice = options[selected];
        if ("Resume".equals(choice)) {
            resumeGameplay();
        } else if ("Restart Level".equals(choice)) {
            gameplayScene.restartLevel();
            resumeGameplay();
        } else if ("Main Menu".equals(choice)) {
            context.getScenes().switchTo(ArkanoidGame.SCENE_MENU);
        } else {
            resumeGameplay();
        }
    }

    private void resumeGameplay() {
        if (gameplayScene != null) {
            gameplayScene.resumeFromPause();
        }
        context.getScenes().switchTo(ArkanoidGame.SCENE_GAMEPLAY);
    }

    @Override
    public void render(Graphics2D graphics) {
        int width = context.getConfig().width();
        int height = context.getConfig().height();

        if (gameplayScene != null) {
            gameplayScene.render(graphics);
        }

        // Semi-transparent overlay
        graphics.setColor(new Color(0, 0, 0, 150));
        graphics.fillRect(0, 0, width, height);

        graphics.setColor(Color.WHITE);
        graphics.setFont(titleFont);
        String title = "Paused";
        int titleWidth = graphics.getFontMetrics().stringWidth(title);
        graphics.drawString(title, (width - titleWidth) / 2, height / 3);

        graphics.setFont(optionFont);
        for (int i = 0; i < options.length; i++) {
            String option = options[i];
            int textWidth = graphics.getFontMetrics().stringWidth(option);
            int x = (width - textWidth) / 2;
            int y = height / 2 + i * 50;
            graphics.setColor(i == selected ? new Color(0xFFEB3B) : Color.LIGHT_GRAY);
            graphics.drawString(option, x, y);
        }
    }
}
