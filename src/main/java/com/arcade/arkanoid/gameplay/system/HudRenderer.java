package com.arcade.arkanoid.gameplay.system;

import com.arcade.arkanoid.gameplay.objectives.ObjectiveEngine;
import com.arcade.arkanoid.localization.LocalizationService;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.List;

public class HudRenderer {
    private final Font labelFont = new Font("SansSerif", Font.BOLD, 20);
    private final Font objectiveFont = new Font("SansSerif", Font.PLAIN, 16);
    private final Font messageFont = new Font("SansSerif", Font.BOLD, 32);
    private final LocalizationService localization;

    public HudRenderer(LocalizationService localization) {
        this.localization = localization;
    }

    public void renderHud(Graphics2D graphics,
                          int score,
                          int lives,
                          String levelName,
                          List<ObjectiveEngine.ObjectiveState> objectives) {
        graphics.setFont(labelFont);
        graphics.setColor(Color.WHITE);
        graphics.drawString(localization.translate("hud.label.score") + ": " + score, 20, 30);
        graphics.drawString(localization.translate("hud.label.lives") + ": " + lives, 180, 30);
        graphics.drawString(localization.translate("hud.label.level") + ": " + levelName, 320, 30);

        if (objectives == null || objectives.isEmpty()) {
            return;
        }

        graphics.setFont(objectiveFont);
        int y = 60;
        int maxEntries = Math.min(objectives.size(), 3);
        for (int i = 0; i < maxEntries; i++) {
            ObjectiveEngine.ObjectiveState state = objectives.get(i);
            graphics.drawString(formatObjectiveLine(state), 20, y);
            y += 18;
        }
    }

    public void renderCenterMessage(Graphics2D graphics, String message, int canvasWidth, int canvasHeight) {
        graphics.setFont(messageFont);
        graphics.setColor(Color.WHITE);
        int textWidth = graphics.getFontMetrics().stringWidth(message);
        int x = (canvasWidth - textWidth) / 2;
        int y = canvasHeight / 2;
        graphics.drawString(message, x, y);
    }

    private String formatObjectiveLine(ObjectiveEngine.ObjectiveState state) {
        String indicatorKey;
        switch (state.status()) {
            case COMPLETED:
                indicatorKey = "hud.objectiveIndicator.completed";
                break;
            case FAILED:
                indicatorKey = "hud.objectiveIndicator.failed";
                break;
            default:
                indicatorKey = "hud.objectiveIndicator.inProgress";
                break;
        }
        String indicator = localization.translate(indicatorKey);
        String labelKey = "objective." + state.id();
        String label = localization.translate(labelKey);
        if (label.equals(labelKey)) {
            label = state.id();
        }
        String progress = state.target() > 0
                ? state.progress() + "/" + state.target()
                : Integer.toString(state.progress());
        return indicator + " " + label + " " + progress;
    }
}
