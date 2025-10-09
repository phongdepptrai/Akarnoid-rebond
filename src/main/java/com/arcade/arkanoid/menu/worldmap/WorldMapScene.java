package com.arcade.arkanoid.menu.worldmap;

import com.arcade.arkanoid.ArkanoidGame;
import com.arcade.arkanoid.engine.core.GameContext;
import com.arcade.arkanoid.engine.input.InputManager;
import com.arcade.arkanoid.engine.scene.Scene;
import com.arcade.arkanoid.gameplay.GameplayScene;
import com.arcade.arkanoid.gameplay.levels.LevelDefinition;
import com.arcade.arkanoid.gameplay.levels.LevelManager;
import com.arcade.arkanoid.localization.LocalizationService;
import com.arcade.arkanoid.profile.PlayerProfile;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Overworld map scene with linear progression. Nodes unlock sequentially and optional gates display requirements.
 */
public class WorldMapScene extends Scene {
    private final LevelManager levelManager = new LevelManager();
    private final List<LevelNode> nodes = new ArrayList<>();
    private final Font nodeFont = new Font("Orbitron", Font.BOLD, 20);
    private final Font infoFont = new Font("SansSerif", Font.PLAIN, 18);
    private final LocalizationService localization;
    private final List<Star> starField = new ArrayList<>();
    private final Random random = new Random();
    private int selectedIndex;
    private String statusMessage = "";
    private double animationTime = 0.0;

    public WorldMapScene(GameContext context) {
        super(context);
        this.localization = context.getLocalizationService();
        generateStarField();
    }

    @Override
    public void onEnter() {
        rebuildNodes();
        selectedIndex = Math.min(selectedIndex, nodes.size() - 1);
        statusMessage = "";
    }

    @Override
    public void update(double deltaTime) {
        if (nodes.isEmpty()) {
            return;
        }
        animationTime += deltaTime;
        InputManager input = context.getInput();
        if (input.isKeyJustPressed(KeyEvent.VK_ESCAPE)) {
            context.getScenes().switchTo(ArkanoidGame.SCENE_MENU);
            return;
        }
        if (input.isKeyJustPressed(KeyEvent.VK_RIGHT) || input.isKeyJustPressed(KeyEvent.VK_D)) {
            selectedIndex = Math.min(selectedIndex + 1, nodes.size() - 1);
            statusMessage = nodes.get(selectedIndex).getGateMessage();
        } else if (input.isKeyJustPressed(KeyEvent.VK_LEFT) || input.isKeyJustPressed(KeyEvent.VK_A)) {
            selectedIndex = Math.max(selectedIndex - 1, 0);
            statusMessage = nodes.get(selectedIndex).getGateMessage();
        } else if (input.isKeyJustPressed(KeyEvent.VK_ENTER) || input.isKeyJustPressed(KeyEvent.VK_SPACE)) {
            attemptStartLevel(nodes.get(selectedIndex));
        }
    }

    @Override
    public void render(Graphics2D graphics) {
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        drawBackground(graphics);
        drawPaths(graphics);
        drawNodes(graphics);
        drawUi(graphics);
    }

    private void rebuildNodes() {
        nodes.clear();
        PlayerProfile profile = context.getProfileManager().getActiveProfile();
        String currentLevelId = profile.getCurrentLevelId();

        int width = context.getConfig().width();
        int startX = 120;
        int total = Math.max(1, levelManager.totalLevels());
        int stepX = total == 1 ? 0 : (width - 240) / (total - 1);
        int centerY = context.getConfig().height() / 2;

        levelManager.reset();
        int selection = 0;
        for (int i = 0; i < total; i++) {
            LevelDefinition definition = levelManager.current();
            int x = startX + i * stepX;
            LevelNode node = new LevelNode(definition, i, new java.awt.Rectangle(x - 30, centerY - 30, 60, 60));
            boolean unlocked = profile.isLevelUnlocked(definition.id());
            node.setUnlocked(unlocked);
            node.setCompleted(profile.isLevelCompleted(definition.id()));
            if (!unlocked) {
                node.setGateMessage(localization.translate("worldMap.locked"));
            } else {
                node.setGateMessage("");
            }
            nodes.add(node);
            if (definition.id().equals(currentLevelId)) {
                selection = i;
            }
            if (i < total - 1) {
                levelManager.advance();
            }
        }
        levelManager.resetToLevel(currentLevelId);
        selectedIndex = selection;
        if (!nodes.isEmpty()) {
            statusMessage = nodes.get(selectedIndex).getGateMessage();
        } else {
            statusMessage = "";
        }
    }

    private void attemptStartLevel(LevelNode node) {
        if (!node.isUnlocked()) {
            statusMessage = node.getGateMessage();
            return;
        }
        if (!context.getEconomyService().consumeLife()) {
            statusMessage = localization.translate("worldMap.noLives");
            return;
        }
        PlayerProfile profile = context.getProfileManager().getActiveProfile();
        profile.setCurrentLevelId(node.getLevelDefinition().id());
        context.getProfileManager().saveProfile();

        levelManager.resetToLevel(node.getLevelDefinition().id());
        GameplayScene gameplay = (GameplayScene) context.getScenes().getPersistentScene(ArkanoidGame.SCENE_GAMEPLAY);
        if (gameplay != null) {
            gameplay.beginNewSession();
            context.getScenes().switchTo(ArkanoidGame.SCENE_GAMEPLAY);
        } else {
            context.getEconomyService().awardLife(1);
            statusMessage = localization.translate("worldMap.loadingError");
        }
    }

    private void drawNodes(Graphics2D graphics) {
        for (int i = 0; i < nodes.size(); i++) {
            LevelNode node = nodes.get(i);
            Rectangle2D bounds = node.getBounds();
            double baseX = bounds.getX();
            double baseY = bounds.getY();
            double width = bounds.getWidth();
            double height = bounds.getHeight();

            if (i == selectedIndex) {
                double pulse = 8 + 4 * Math.sin(animationTime * 4);
                Ellipse2D halo = new Ellipse2D.Double(
                        baseX - pulse,
                        baseY - pulse,
                        width + pulse * 2,
                        height + pulse * 2
                );
                graphics.setColor(new Color(255, 255, 255, 40));
                graphics.fill(halo);
            }

            Ellipse2D circle = new Ellipse2D.Double(baseX, baseY, width, height);
            Color fill;
            if (!node.isUnlocked()) {
                fill = new Color(80, 80, 90);
            } else if (node.isCompleted()) {
                fill = new Color(0x4CAF50);
            } else {
                fill = new Color(0xFFC107);
            }
            graphics.setColor(fill);
            graphics.fill(circle);

            graphics.setStroke(new BasicStroke(i == selectedIndex ? 6 : 3));
            graphics.setColor(i == selectedIndex ? Color.WHITE : new Color(30, 30, 40));
            graphics.draw(circle);

            graphics.setFont(nodeFont);
            graphics.setColor(Color.BLACK);
            String label = node.getLevelDefinition().displayName();
            int textWidth = graphics.getFontMetrics().stringWidth(label);
            int textX = (int) (baseX + (width - textWidth) / 2);
            int textY = (int) (baseY + height + 40);
            graphics.drawString(label, textX, textY);

            if (node.isCompleted()) {
                graphics.setColor(new Color(255, 255, 255, 200));
                graphics.setStroke(new BasicStroke(3));
                int cx = (int) (baseX + width / 2);
                int cy = (int) (baseY + height / 2);
                graphics.drawLine(cx - 10, cy, cx - 2, cy + 10);
                graphics.drawLine(cx - 2, cy + 10, cx + 12, cy - 8);
            }
        }
    }

    private void drawPaths(Graphics2D graphics) {
        if (nodes.size() < 2) {
            return;
        }
        graphics.setStroke(new BasicStroke(8, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < nodes.size() - 1; i++) {
            LevelNode from = nodes.get(i);
            LevelNode to = nodes.get(i + 1);
            graphics.setColor(from.isUnlocked() && to.isUnlocked() ? new Color(0x4CAF50) : new Color(90, 90, 110));
            int x1 = from.getBounds().x + from.getBounds().width / 2;
            int y1 = from.getBounds().y + from.getBounds().height / 2;
            int x2 = to.getBounds().x + to.getBounds().width / 2;
            int y2 = to.getBounds().y + to.getBounds().height / 2;
            graphics.drawLine(x1, y1, x2, y2);
        }
    }

    private void drawUi(Graphics2D graphics) {
        PlayerProfile profile = context.getProfileManager().getActiveProfile();
        graphics.setFont(infoFont);
        graphics.setColor(Color.WHITE);
        graphics.drawString(localization.translate("worldMap.label.lives", profile.getLives(), profile.getMaxLives()), 40, 60);
        graphics.drawString(localization.translate("worldMap.label.coins", profile.getCoins()), 40, 90);
        graphics.drawString(localization.translate("worldMap.label.energy", profile.getEnergy(), profile.getMaxEnergy()), 40, 120);
        graphics.drawString(localization.translate("worldMap.label.streak", profile.getDailyStreak()), 40, 150);

        if (statusMessage != null && !statusMessage.isBlank()) {
            graphics.drawString(statusMessage, 40, context.getConfig().height() - 80);
        }
        graphics.drawString(localization.translate("worldMap.instructions"), 40, context.getConfig().height() - 40);
    }

    private void drawBackground(Graphics2D graphics) {
        int width = context.getConfig().width();
        int height = context.getConfig().height();
        Color top = new Color(10, 15, 35);
        Color bottom = new Color(5, 5, 15);
        graphics.setPaint(new java.awt.GradientPaint(0, 0, top, 0, height, bottom));
        graphics.fillRect(0, 0, width, height);
        for (Star star : starField) {
            float alpha = (float) (star.baseAlpha + 0.3 * Math.sin(animationTime * star.twinkleSpeed + star.phase));
            alpha = Math.max(0.05f, Math.min(alpha, 1f));
            graphics.setColor(new Color(1f, 1f, 1f, alpha));
            graphics.fillRect(star.x, star.y, star.size, star.size);
        }
        graphics.setColor(new Color(38, 50, 56, 180));
        graphics.setStroke(new BasicStroke(4));
        graphics.drawRoundRect(30, 40, width - 60, height - 80, 30, 30);
    }

    private void generateStarField() {
        int width = context.getConfig().width();
        int height = context.getConfig().height();
        int count = 90;
        for (int i = 0; i < count; i++) {
            Star star = new Star();
            star.x = random.nextInt(Math.max(1, width));
            star.y = random.nextInt(Math.max(1, height));
            star.size = random.nextInt(2) + 1;
            star.twinkleSpeed = 1.5 + random.nextDouble() * 2.5;
            star.phase = random.nextDouble() * Math.PI * 2;
            star.baseAlpha = 0.4f + random.nextFloat() * 0.4f;
            starField.add(star);
        }
    }

    private static final class Star {
        int x;
        int y;
        int size;
        double twinkleSpeed;
        double phase;
        float baseAlpha;
    }
}
