package com.arcade.arkanoid.menu.save;

import com.arcade.arkanoid.ArkanoidGame;
import com.arcade.arkanoid.engine.core.GameContext;
import com.arcade.arkanoid.engine.input.InputManager;
import com.arcade.arkanoid.engine.scene.Scene;
import com.arcade.arkanoid.localization.LocalizationService;
import com.arcade.arkanoid.profile.PlayerProfile;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SaveMenuScene extends Scene {
    private static final int MAX_SLOTS = 4;
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    private final SampleSaveRepository repository = new SampleSaveRepository();
    private final LocalizationService localization;
    private final Font titleFont = new Font("Orbitron", Font.BOLD, 48);
    private final Font slotFont = new Font("SansSerif", Font.BOLD, 26);
    private final Font detailFont = new Font("SansSerif", Font.PLAIN, 18);
    private final Font hintFont = new Font("SansSerif", Font.PLAIN, 16);

    private List<SaveSlotSummary> slots = new ArrayList<>();
    private int selectedIndex;
    private String statusMessage = "";

    public SaveMenuScene(GameContext context) {
        super(context);
        this.localization = context.getLocalizationService();
    }

    @Override
    public void onEnter() {
        slots = repository.loadSlots(MAX_SLOTS);
        if (slots.isEmpty()) {
            for (int i = 1; i <= MAX_SLOTS; i++) {
                slots.add(new SaveSlotSummary(i, null, 0));
            }
        }
        selectedIndex = Math.min(selectedIndex, slots.size() - 1);
        statusMessage = "";
    }

    @Override
    public void update(double deltaTime) {
        InputManager input = context.getInput();
        if (input.isKeyJustPressed(KeyEvent.VK_ESCAPE)) {
            context.getScenes().switchTo(ArkanoidGame.SCENE_MENU);
            return;
        }
        if (input.isKeyJustPressed(KeyEvent.VK_UP) || input.isKeyJustPressed(KeyEvent.VK_W)) {
            selectedIndex = Math.max(0, selectedIndex - 1);
        } else if (input.isKeyJustPressed(KeyEvent.VK_DOWN) || input.isKeyJustPressed(KeyEvent.VK_S)) {
            selectedIndex = Math.min(slots.size() - 1, selectedIndex + 1);
        } else if (input.isKeyJustPressed(KeyEvent.VK_DELETE) || input.isKeyJustPressed(KeyEvent.VK_BACK_SPACE)) {
            deleteSelectedSlot();
        } else if (input.isKeyJustPressed(KeyEvent.VK_ENTER) || input.isKeyJustPressed(KeyEvent.VK_SPACE)) {
            activateSelectedSlot();
        }
    }

    private void activateSelectedSlot() {
        if (slots.isEmpty()) {
            return;
        }
        SaveSlotSummary summary = slots.get(selectedIndex);
        if (summary.isOccupied()) {
            PlayerProfile profile = summary.getProfile();
            context.getProfileManager().refreshProfile(profile);
            repository.writeSlot(summary.getSlotId(), profile);
            slots.set(selectedIndex, new SaveSlotSummary(summary.getSlotId(), profile, System.currentTimeMillis() / 1000));
            statusMessage = localization.translate("saveMenu.slotLoaded", profile.getDisplayName());
            context.getScenes().switchTo(ArkanoidGame.SCENE_MAP);
        } else {
            PlayerProfile profile = PlayerProfile.newDefault();
            profile.setDisplayName(localization.translate("saveMenu.defaultName", summary.getSlotId()));
            repository.writeSlot(summary.getSlotId(), profile);
            context.getProfileManager().refreshProfile(profile);
            slots.set(selectedIndex, new SaveSlotSummary(summary.getSlotId(), profile, System.currentTimeMillis() / 1000));
            statusMessage = localization.translate("saveMenu.slotCreated", profile.getDisplayName());
            context.getScenes().switchTo(ArkanoidGame.SCENE_MAP);
        }
    }

    private void deleteSelectedSlot() {
        if (slots.isEmpty()) {
            return;
        }
        SaveSlotSummary summary = slots.get(selectedIndex);
        if (!summary.isOccupied()) {
            statusMessage = localization.translate("saveMenu.empty");
            return;
        }
        repository.deleteSlot(summary.getSlotId());
        slots.set(selectedIndex, new SaveSlotSummary(summary.getSlotId(), null, 0));
        statusMessage = localization.translate("saveMenu.slotDeleted", summary.getDisplayName());
    }

    @Override
    public void render(Graphics2D graphics) {
        drawBackground(graphics);
        int width = context.getConfig().width();
        graphics.setFont(titleFont);
        graphics.setColor(Color.WHITE);
        String title = localization.translate("saveMenu.title");
        int titleWidth = graphics.getFontMetrics().stringWidth(title);
        graphics.drawString(title, (width - titleWidth) / 2, 120);

        int slotAreaTop = 180;
        int slotHeight = 130;
        for (int i = 0; i < slots.size(); i++) {
            int y = slotAreaTop + i * slotHeight;
            drawSlot(graphics, slots.get(i), y, i == selectedIndex);
        }

        graphics.setFont(detailFont);
        graphics.setColor(new Color(200, 200, 200));
        String instructions = localization.translate("saveMenu.instructions");
        graphics.drawString(instructions, 40, context.getConfig().height() - 50);

        if (statusMessage != null && !statusMessage.isBlank()) {
            graphics.setFont(detailFont);
            graphics.setColor(new Color(255, 235, 59));
            graphics.drawString(statusMessage, 40, context.getConfig().height() - 80);
        }
    }

    private void drawSlot(Graphics2D graphics, SaveSlotSummary summary, int y, boolean selected) {
        int width = context.getConfig().width() - 80;
        int x = 40;
        int height = 110;
        graphics.setColor(selected ? new Color(66, 165, 245, 160) : new Color(30, 30, 40, 180));
        graphics.fillRoundRect(x, y, width, height, 20, 20);
        graphics.setColor(selected ? new Color(187, 222, 251) : new Color(84, 110, 122));
        graphics.setStroke(new BasicStroke(selected ? 4 : 2));
        graphics.drawRoundRect(x, y, width, height, 20, 20);

        String slotLabel = localization.translate("saveMenu.slotLabel", summary.getSlotId(),
                summary.isOccupied() ? summary.getDisplayName() : localization.translate("saveMenu.emptyName"));
        graphics.setFont(slotFont);
        graphics.setColor(Color.WHITE);
        graphics.drawString(slotLabel, x + 20, y + 32);

        graphics.setFont(detailFont);
        int lineY = y + 56;
        if (summary.isOccupied() && summary.getProfile() != null) {
            PlayerProfile profile = summary.getProfile();
            String summaryLine = localization.translate(
                    "saveMenu.summary",
                    profile.getCurrentLevelId(),
                    profile.getLives(),
                    profile.getCoins(),
                    profile.getDailyStreak()
            );
            graphics.setColor(new Color(220, 220, 220));
            graphics.drawString(summaryLine, x + 20, lineY);

            String paddleName = localization.translate("shop.item.paddle." + summary.getActivePaddleSkin());
            String ballName = localization.translate("shop.item.ball." + summary.getActiveBallSkin());
            String skinLine = localization.translate(
                    "saveMenu.summarySkins",
                    paddleName,
                    summary.getOwnedPaddleSkins(),
                    ballName,
                    summary.getOwnedBallSkins()
            );
            graphics.setColor(new Color(200, 210, 220));
            graphics.drawString(skinLine, x + 20, lineY + 20);

            String lastPlayed = summary.getLastPlayedEpochSeconds() > 0
                    ? DATE_FORMAT.format(Instant.ofEpochSecond(summary.getLastPlayedEpochSeconds()))
                    : localization.translate("saveMenu.neverPlayed");
            graphics.setColor(new Color(180, 200, 200));
            graphics.drawString(localization.translate("saveMenu.lastPlayed", lastPlayed), x + 20, lineY + 40);
        } else {
            graphics.setColor(new Color(200, 200, 200));
            graphics.drawString(localization.translate("saveMenu.empty"), x + 20, lineY);
            graphics.setColor(new Color(160, 180, 200));
            graphics.drawString(localization.translate("saveMenu.emptyHint"), x + 20, lineY + 20);
        }
    }

    private void drawBackground(Graphics2D graphics) {
        int width = context.getConfig().width();
        int height = context.getConfig().height();
        Color top = new Color(18, 18, 30);
        Color bottom = new Color(3, 3, 12);
        graphics.setPaint(new java.awt.GradientPaint(0, 0, top, 0, height, bottom));
        graphics.fillRect(0, 0, width, height);
        graphics.setColor(new Color(255, 255, 255, 20));
        graphics.fillRect(0, 0, width, height);
    }
}
