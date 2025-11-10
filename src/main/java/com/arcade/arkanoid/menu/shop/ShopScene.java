package com.arcade.arkanoid.menu.shop;

import com.arcade.arkanoid.ArkanoidGame;
import com.arcade.arkanoid.engine.core.GameContext;
import com.arcade.arkanoid.engine.input.InputManager;
import com.arcade.arkanoid.engine.scene.Scene;
import com.arcade.arkanoid.engine.audio.BackgroundMusicManager;
import com.arcade.arkanoid.economy.EconomyService;
import com.arcade.arkanoid.gameplay.cosmetics.SkinCatalog;
import com.arcade.arkanoid.localization.LocalizationService;
import com.arcade.arkanoid.profile.PlayerProfile;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShopScene extends Scene {
    private enum ViewMode {
        CATEGORY,
        PADDLE,
        BALL,
        BUFF
    }

    private enum ItemType {
        PADDLE,
        BALL,
        BUFF
    }

    private static final class ShopItem {
        final String id;
        final ItemType type;
        final int price;
        final String nameKey;
        final String descriptionKey;
        final int rewardCoins;
        final int rewardLives;
        final int rewardEnergy;

        ShopItem(String id, ItemType type, int price, String nameKey) {
            this(id, type, price, nameKey, null, 0, 0, 0);
        }

        ShopItem(String id, ItemType type, int price, String nameKey, String descriptionKey,
                int rewardCoins, int rewardLives, int rewardEnergy) {
            this.id = id;
            this.type = type;
            this.price = price;
            this.nameKey = nameKey;
            this.descriptionKey = descriptionKey;
            this.rewardCoins = rewardCoins;
            this.rewardLives = rewardLives;
            this.rewardEnergy = rewardEnergy;
        }

        boolean isSkin() {
            return type == ItemType.PADDLE || type == ItemType.BALL;
        }
    }

    private static final class CategoryEntry {
        final ViewMode view;
        final String labelKey;

        CategoryEntry(ViewMode view, String labelKey) {
            this.view = view;
            this.labelKey = labelKey;
        }
    }

    private static final double ITEM_SPACING = 120.0;
    private static final double SCROLL_SPEED = 10.0;
    private static final int VISIBLE_COUNT = 3;

    private final LocalizationService localization;
    private final EconomyService economyService;
    private final Font titleFont = new Font("Orbitron", Font.BOLD, 48);
    private final Font sectionFont = new Font("Orbitron", Font.BOLD, 26);
    private final Font categoryFont = new Font("Orbitron", Font.BOLD, 30);
    private final Font itemFont = new Font("SansSerif", Font.BOLD, 22);
    private final Font detailFont = new Font("SansSerif", Font.PLAIN, 18);

    private final List<CategoryEntry> categories = Arrays.asList(
            new CategoryEntry(ViewMode.PADDLE, "shop.category.paddle"),
            new CategoryEntry(ViewMode.BALL, "shop.category.ball"),
            new CategoryEntry(ViewMode.BUFF, "shop.category.buff"));
    private final List<ShopItem> paddleItems = new ArrayList<>();
    private final List<ShopItem> ballItems = new ArrayList<>();
    private final List<ShopItem> buffItems = new ArrayList<>();

    private ViewMode viewMode = ViewMode.CATEGORY;
    private int categoryIndex = 0;
    private int itemIndex = 0;
    private double scrollOffset = 0.0;
    private double targetScrollOffset = 0.0;
    private String statusMessage = "";

    public ShopScene(GameContext context) {
        super(context);
        this.localization = context.getLocalizationService();
        this.economyService = context.getEconomyService();
        bootstrapItems();
    }

    private void bootstrapItems() {
        paddleItems.clear();
        paddleItems.add(new ShopItem("classic", ItemType.PADDLE, 0, "shop.item.paddle.classic"));
        paddleItems.add(new ShopItem("neon-stream", ItemType.PADDLE, 300, "shop.item.paddle.neon"));
        paddleItems.add(new ShopItem("retro-grid", ItemType.PADDLE, 450, "shop.item.paddle.retro"));
        paddleItems.add(new ShopItem("sunset-glow", ItemType.PADDLE, 520, "shop.item.paddle.sunset"));
        paddleItems.add(new ShopItem("frost-byte", ItemType.PADDLE, 480, "shop.item.paddle.frost"));

        ballItems.clear();
        ballItems.add(new ShopItem("classic", ItemType.BALL, 0, "shop.item.ball.classic"));
        ballItems.add(new ShopItem("ion-burst", ItemType.BALL, 250, "shop.item.ball.ion"));
        ballItems.add(new ShopItem("supernova", ItemType.BALL, 400, "shop.item.ball.supernova"));
        ballItems.add(new ShopItem("aurora", ItemType.BALL, 320, "shop.item.ball.aurora"));
        ballItems.add(new ShopItem("quantum-core", ItemType.BALL, 500, "shop.item.ball.quantum"));

        buffItems.clear();
        buffItems.add(new ShopItem("buff-extra-lives", ItemType.BUFF, 220,
                "shop.item.buff.extraLives", "shop.item.buff.extraLives.desc", 0, 2, 0));
        buffItems.add(new ShopItem("buff-energy-charge", ItemType.BUFF, 180,
                "shop.item.buff.energy", "shop.item.buff.energy.desc", 0, 0, 10));
        buffItems.add(new ShopItem("buff-coin-pack", ItemType.BUFF, 250,
                "shop.item.buff.coinPack", "shop.item.buff.coinPack.desc", 400, 0, 0));
    }

    @Override
    public void onEnter() {
        statusMessage = "";
        setView(ViewMode.CATEGORY);

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
        InputManager input = context.getInput();
        if (viewMode == ViewMode.CATEGORY) {
            handleCategoryInput(input);
        } else {
            handleItemInput(input);
        }
        if (viewMode != ViewMode.CATEGORY) {
            double diff = targetScrollOffset - scrollOffset;
            if (Math.abs(diff) > 0.5) {
                scrollOffset += diff * Math.min(1.0, deltaTime * SCROLL_SPEED);
            } else {
                scrollOffset = targetScrollOffset;
            }
        }
    }

    private void handleCategoryInput(InputManager input) {
        if (input.isKeyJustPressed(KeyEvent.VK_ESCAPE)) {
            context.getScenes().switchTo(ArkanoidGame.SCENE_MENU);
            return;
        }
        if (input.isKeyJustPressed(KeyEvent.VK_UP) || input.isKeyJustPressed(KeyEvent.VK_W)) {
            categoryIndex = (categoryIndex - 1 + categories.size()) % categories.size();
        } else if (input.isKeyJustPressed(KeyEvent.VK_DOWN) || input.isKeyJustPressed(KeyEvent.VK_S)) {
            categoryIndex = (categoryIndex + 1) % categories.size();
        } else if (input.isKeyJustPressed(KeyEvent.VK_ENTER) || input.isKeyJustPressed(KeyEvent.VK_SPACE)) {
            setView(categories.get(categoryIndex).view);
        }
    }

    private void handleItemInput(InputManager input) {
        if (input.isKeyJustPressed(KeyEvent.VK_ESCAPE)) {
            setView(ViewMode.CATEGORY);
            return;
        }
        List<ShopItem> list = currentItemList();
        if (list.isEmpty()) {
            return;
        }
        if (input.isKeyJustPressed(KeyEvent.VK_UP) || input.isKeyJustPressed(KeyEvent.VK_W)) {
            setItemIndex(Math.max(0, itemIndex - 1));
        } else if (input.isKeyJustPressed(KeyEvent.VK_DOWN) || input.isKeyJustPressed(KeyEvent.VK_S)) {
            setItemIndex(Math.min(list.size() - 1, itemIndex + 1));
        } else if (input.isKeyJustPressed(KeyEvent.VK_ENTER) || input.isKeyJustPressed(KeyEvent.VK_SPACE)) {
            purchase(list.get(itemIndex));
        }
    }

    private void setItemIndex(int newIndex) {
        itemIndex = newIndex;
        int centerIndex = VISIBLE_COUNT / 2;
        double desired = Math.max(0, (itemIndex - centerIndex) * ITEM_SPACING);
        double maxOffset = Math.max(0, (currentItemList().size() - VISIBLE_COUNT) * ITEM_SPACING);
        targetScrollOffset = Math.max(0, Math.min(desired, maxOffset));
    }

    private void setView(ViewMode newMode) {
        viewMode = newMode;
        if (viewMode == ViewMode.CATEGORY) {
            statusMessage = "";
        } else {
            itemIndex = 0;
            scrollOffset = 0;
            targetScrollOffset = 0;
            statusMessage = "";
        }
    }

    private List<ShopItem> currentItemList() {
        switch (viewMode) {
            case PADDLE:
                return paddleItems;
            case BALL:
                return ballItems;
            case BUFF:
                return buffItems;
            default:
                return new ArrayList<>();
        }
    }

    private void purchase(ShopItem item) {
        PlayerProfile profile = context.getProfileManager().getActiveProfile();
        if (item.type == ItemType.BUFF) {
            if (item.price > 0 && !economyService.spendCoins(item.price)) {
                statusMessage = localization.translate("shop.notEnough");
                return;
            }
            if (item.rewardLives > 0) {
                economyService.awardLife(item.rewardLives);
            }
            if (item.rewardEnergy > 0) {
                economyService.awardEnergy(item.rewardEnergy);
            }
            if (item.rewardCoins > 0) {
                economyService.addCoins(item.rewardCoins);
            }
            statusMessage = localization.translate("shop.buff.applied", localization.translate(item.nameKey));
            return;
        }

        boolean owned = item.type == ItemType.PADDLE
                ? profile.hasPaddleSkin(item.id)
                : profile.hasBallSkin(item.id);
        if (!owned) {
            if (item.price > 0 && !economyService.spendCoins(item.price)) {
                statusMessage = localization.translate("shop.notEnough");
                return;
            }
            if (item.type == ItemType.PADDLE) {
                profile.addPaddleSkin(item.id);
            } else {
                profile.addBallSkin(item.id);
            }
            statusMessage = localization.translate("shop.purchased", localization.translate(item.nameKey));
        }

        if (item.type == ItemType.PADDLE) {
            profile.setActivePaddleSkin(item.id);
        } else {
            profile.setActiveBallSkin(item.id);
        }
        context.getProfileManager().saveProfile();
        statusMessage = localization.translate("shop.equipped", localization.translate(item.nameKey));
    }

    @Override
    public void render(Graphics2D graphics) {
        drawBackground(graphics);
        int width = context.getConfig().width();
        graphics.setFont(titleFont);
        graphics.setColor(Color.WHITE);
        String title = localization.translate("shop.title");
        graphics.drawString(title, (width - graphics.getFontMetrics().stringWidth(title)) / 2, 110);

        graphics.setFont(sectionFont);
        graphics.setColor(new Color(200, 220, 255));
        graphics.drawString(
                localization.translate("shop.coins", context.getProfileManager().getActiveProfile().getCoins()), 50,
                160);

        if (viewMode == ViewMode.CATEGORY) {
            renderCategoryMenu(graphics);
        } else {
            renderItemList(graphics);
        }
    }

    private void renderCategoryMenu(Graphics2D graphics) {
        int baseY = 230;
        int rowHeight = 100;
        graphics.setFont(categoryFont);
        for (int i = 0; i < categories.size(); i++) {
            boolean selected = (i == categoryIndex);
            int y = baseY + i * rowHeight;
            drawCategoryButton(graphics, categories.get(i), y, selected);
        }
        graphics.setFont(detailFont);
        graphics.setColor(new Color(190, 190, 190));
        graphics.drawString(localization.translate("shop.instructions.category"), 40,
                context.getConfig().height() - 50);
    }

    private void drawCategoryButton(Graphics2D graphics, CategoryEntry entry, int y, boolean selected) {
        int x = 80;
        int width = context.getConfig().width() - 160;
        int height = 80;
        graphics.setColor(selected ? new Color(129, 212, 250, 180) : new Color(40, 40, 60, 200));
        graphics.fillRoundRect(x, y, width, height, 18, 18);
        graphics.setColor(selected ? new Color(187, 222, 251) : new Color(84, 110, 122));
        graphics.setStroke(new BasicStroke(selected ? 4 : 2));
        graphics.drawRoundRect(x, y, width, height, 18, 18);
        graphics.setColor(Color.WHITE);
        graphics.drawString(localization.translate(entry.labelKey), x + 30, y + 48);
    }

    private void renderItemList(Graphics2D graphics) {
        List<ShopItem> list = currentItemList();
        if (list.isEmpty()) {
            graphics.setFont(detailFont);
            graphics.setColor(Color.WHITE);
            graphics.drawString(localization.translate("shop.empty"), 80, 240);
            return;
        }
        graphics.setFont(itemFont);
        int baseY = 220;
        for (int i = 0; i < list.size(); i++) {
            double y = baseY + i * ITEM_SPACING - scrollOffset;
            if (y < baseY - ITEM_SPACING || y > baseY + ITEM_SPACING * VISIBLE_COUNT) {
                continue;
            }
            drawItem(graphics, list.get(i), (int) y, i == itemIndex);
        }
        graphics.setFont(detailFont);
        graphics.setColor(new Color(190, 190, 190));
        graphics.drawString(localization.translate("shop.instructions.items"), 40,
                context.getConfig().height() - 50);
        if (statusMessage != null && !statusMessage.isBlank()) {
            graphics.setColor(new Color(255, 235, 59));
            graphics.drawString(statusMessage, 40, context.getConfig().height() - 80);
        }
    }

    private void drawItem(Graphics2D graphics, ShopItem item, int y, boolean selected) {
        int x = 60;
        int width = context.getConfig().width() - 120;
        int height = 100;
        graphics.setColor(selected ? new Color(129, 212, 250, 160) : new Color(33, 33, 44, 180));
        graphics.fillRoundRect(x, y, width, height, 18, 18);
        graphics.setColor(selected ? new Color(187, 222, 251) : new Color(84, 110, 122));
        graphics.setStroke(new BasicStroke(selected ? 4 : 2));
        graphics.drawRoundRect(x, y, width, height, 18, 18);

        int previewX = x + 30;
        int previewY = y + 20;
        switch (item.type) {
            case PADDLE:
                SkinCatalog.PaddleSkin paddleSkin = SkinCatalog.paddleSkin(item.id);
                graphics.setColor(paddleSkin.fillColor());
                graphics.fillRoundRect(previewX, previewY + 12, 130, 22, 16, 16);
                graphics.setColor(paddleSkin.borderColor());
                graphics.setStroke(new BasicStroke(3));
                graphics.drawRoundRect(previewX, previewY + 12, 130, 22, 16, 16);
                break;
            case BALL:
                SkinCatalog.BallSkin ballSkin = SkinCatalog.ballSkin(item.id);
                graphics.setColor(ballSkin.fillColor());
                graphics.fillOval(previewX + 40, previewY, 38, 38);
                graphics.setColor(ballSkin.borderColor());
                graphics.setStroke(new BasicStroke(3));
                graphics.drawOval(previewX + 40, previewY, 38, 38);
                break;
            case BUFF:
                graphics.setColor(new Color(255, 214, 0, 200));
                graphics.fillPolygon(
                        new int[] { previewX + 60, previewX + 85, previewX + 70 },
                        new int[] { previewY, previewY + 18, previewY + 50 },
                        3);
                graphics.setColor(new Color(255, 255, 255, 220));
                graphics.setStroke(new BasicStroke(3));
                graphics.drawPolygon(
                        new int[] { previewX + 60, previewX + 85, previewX + 70 },
                        new int[] { previewY, previewY + 18, previewY + 50 },
                        3);
                break;
            default:
                break;
        }

        int textX = previewX + 170;
        graphics.setFont(itemFont);
        graphics.setColor(Color.WHITE);
        graphics.drawString(localization.translate(item.nameKey), textX, y + 38);

        graphics.setFont(detailFont);
        if (item.isSkin()) {
            PlayerProfile profile = context.getProfileManager().getActiveProfile();
            boolean owned = item.type == ItemType.PADDLE
                    ? profile.hasPaddleSkin(item.id)
                    : profile.hasBallSkin(item.id);
            boolean equipped = item.type == ItemType.PADDLE
                    ? item.id.equals(profile.getActivePaddleSkin())
                    : item.id.equals(profile.getActiveBallSkin());
            drawOwnershipDetails(graphics, owned, equipped, item, x, width, y);
        } else {
            if (item.descriptionKey != null) {
                graphics.setColor(new Color(210, 210, 210));
                graphics.drawString(localization.translate(item.descriptionKey), textX, y + 62);
            }
            graphics.setColor(new Color(255, 213, 79));
            graphics.drawString(localization.translate("shop.price", item.price), x + width - 220, y + 38);
        }
    }

    private void drawOwnershipDetails(Graphics2D graphics, boolean owned, boolean equipped,
            ShopItem item, int x, int width, int y) {
        if (owned) {
            graphics.setColor(equipped ? new Color(129, 199, 132) : new Color(224, 224, 224));
            String labelKey = equipped ? "shop.status.equipped" : "shop.status.owned";
            graphics.drawString(localization.translate(labelKey), x + width - 220, y + 38);
        } else {
            graphics.setColor(new Color(255, 213, 79));
            graphics.drawString(localization.translate("shop.price", item.price), x + width - 220, y + 38);
        }
    }

    private void drawBackground(Graphics2D graphics) {
        int width = context.getConfig().width();
        int height = context.getConfig().height();
        Color top = new Color(15, 15, 28);
        Color bottom = new Color(5, 5, 15);
        graphics.setPaint(new java.awt.GradientPaint(0, 0, top, 0, height, bottom));
        graphics.fillRect(0, 0, width, height);
        graphics.setColor(new Color(255, 255, 255, 25));
        graphics.fillRect(0, 0, width, height);
    }
}
