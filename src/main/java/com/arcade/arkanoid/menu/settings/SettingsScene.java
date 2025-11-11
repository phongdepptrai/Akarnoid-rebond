package com.arcade.arkanoid.menu.settings;

import com.arcade.arkanoid.ArkanoidGame;
import com.arcade.arkanoid.engine.audio.BackgroundMusicManager;
import com.arcade.arkanoid.engine.audio.StageMusicManager;
import com.arcade.arkanoid.engine.core.GameContext;
import com.arcade.arkanoid.engine.input.InputManager;
import com.arcade.arkanoid.engine.scene.Scene;
import com.arcade.arkanoid.engine.settings.SettingsManager;
import com.arcade.arkanoid.localization.LocalizationService;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Settings scene allows adjusting music volume and language preferences.
 */
public class SettingsScene extends Scene {
    private enum Option {
        MUSIC_VOLUME,
        LANGUAGE,
        BACK
    }

    private static final class LanguageOption {
        final Locale locale;
        final String labelKey;

        LanguageOption(Locale locale, String labelKey) {
            this.locale = locale;
            this.labelKey = labelKey;
        }
    }

    private static final double STATUS_DURATION = 3.0;

    private final LocalizationService localization;
    private final SettingsManager settings;
    private final List<LanguageOption> languages = Arrays.asList(
            new LanguageOption(Locale.ENGLISH, "settings.language.option.en"),
            new LanguageOption(new Locale("vi"), "settings.language.option.vi"));

    private final Font titleFont = new Font("BoldPixels", Font.BOLD, 46);
    private final Font optionFont = new Font("BoldPixels", Font.PLAIN, 28);
    private final Font valueFont = new Font("BoldPixels", Font.BOLD, 20);
    private final Font infoFont = new Font("BoldPixels", Font.PLAIN, 16);

    private int selectedIndex;
    private int musicVolume;
    private int languageIndex;
    private double statusTimer;
    private String statusMessage = "";

    public SettingsScene(GameContext context) {
        super(context);
        this.localization = context.getLocalizationService();
        this.settings = context.getSettingsManager();
    }

    @Override
    public void onEnter() {
        musicVolume = settings.getMusicVolume();
        languageIndex = resolveLanguageIndex(localization.getActiveLocale());
        selectedIndex = 0;
        statusMessage = "";
        statusTimer = 0;
    }

    @Override
    public void update(double deltaTime) {
        statusTimer = Math.max(0, statusTimer - deltaTime);
        InputManager input = context.getInput();

        if (input.isKeyJustPressed(KeyEvent.VK_ESCAPE)) {
            context.getScenes().switchTo(ArkanoidGame.SCENE_MENU);
            return;
        }

        int vertical = input.isKeyJustPressed(KeyEvent.VK_UP) || input.isKeyJustPressed(KeyEvent.VK_W) ? -1
                : input.isKeyJustPressed(KeyEvent.VK_DOWN) || input.isKeyJustPressed(KeyEvent.VK_S) ? 1 : 0;
        if (vertical != 0) {
            selectedIndex = (selectedIndex + vertical + Option.values().length) % Option.values().length;
        }

        int horizontal = input.isKeyJustPressed(KeyEvent.VK_LEFT) || input.isKeyJustPressed(KeyEvent.VK_A) ? -1
                : input.isKeyJustPressed(KeyEvent.VK_RIGHT) || input.isKeyJustPressed(KeyEvent.VK_D) ? 1 : 0;
        if (horizontal != 0) {
            adjustCurrentOption(horizontal);
        }

        if (input.isKeyJustPressed(KeyEvent.VK_ENTER) || input.isKeyJustPressed(KeyEvent.VK_SPACE)) {
            handleSelection();
        }
    }

    @Override
    public void render(Graphics2D g) {
        int width = context.getConfig().width();
        int height = context.getConfig().height();

        g.setColor(new Color(8, 10, 25));
        g.fillRect(0, 0, width, height);

        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        drawTitle(g, width);

        int startY = height / 2 - 80;
        Option[] options = Option.values();
        for (int i = 0; i < options.length; i++) {
            drawOption(g, options[i], i == selectedIndex, width / 2, startY + i * 90);
        }

        drawStatus(g, width, height);
    }

    private void drawTitle(Graphics2D g, int width) {
        String title = localization.translate("settings.title");
        g.setFont(titleFont);
        g.setColor(Color.WHITE);
        int textWidth = g.getFontMetrics().stringWidth(title);
        g.drawString(title, (width - textWidth) / 2, 120);
    }

    private void drawOption(Graphics2D g, Option option, boolean selected, int centerX, int centerY) {
        g.setFont(optionFont);
        g.setColor(selected ? new Color(255, 170, 45) : new Color(190, 190, 210));
        String label;
        switch (option) {
            case MUSIC_VOLUME:
                label = localization.translate("settings.musicVolume");
                break;
            case LANGUAGE:
                label = localization.translate("settings.language");
                break;
            case BACK:
                label = localization.translate("settings.back");
                break;
            default:
                label = option.name();
        }
        int labelWidth = g.getFontMetrics().stringWidth(label);
        g.drawString(label, centerX - labelWidth / 2, centerY);

        g.setFont(valueFont);
        switch (option) {
            case MUSIC_VOLUME:
                drawVolumeBar(g, centerX, centerY + 20);
                break;
            case LANGUAGE:
                String value = localization.translate(languages.get(languageIndex).labelKey);
                int valueWidth = g.getFontMetrics().stringWidth(value);
                g.setColor(Color.WHITE);
                g.drawString(value, centerX - valueWidth / 2, centerY + 20);
                break;
            default:
                break;
        }
    }

    private void drawVolumeBar(Graphics2D g, int centerX, int y) {
        int barWidth = 320;
        int barHeight = 14;
        int x = centerX - barWidth / 2;
        g.setColor(new Color(50, 60, 90));
        g.fillRoundRect(x, y, barWidth, barHeight, 10, 10);
        g.setColor(new Color(70, 80, 110));
        g.setStroke(new BasicStroke(2f));
        g.drawRoundRect(x, y, barWidth, barHeight, 10, 10);

        int filled = (int) (barWidth * (musicVolume / 100.0));
        g.setColor(new Color(86, 204, 242));
        g.fillRoundRect(x, y, filled, barHeight, 10, 10);

        String valueLabel = musicVolume + "%";
        int valueWidth = g.getFontMetrics().stringWidth(valueLabel);
        g.setColor(Color.WHITE);
        g.drawString(valueLabel, centerX - valueWidth / 2, y + barHeight + 20);
    }

    private void drawStatus(Graphics2D g, int width, int height) {
        g.setFont(infoFont);
        g.setColor(new Color(180, 180, 200));
        String instructions = localization.translate("settings.instructions");
        int instrWidth = g.getFontMetrics().stringWidth(instructions);
        g.drawString(instructions, (width - instrWidth) / 2, height - 60);

        if (statusTimer > 0 && !statusMessage.isEmpty()) {
            g.setColor(new Color(120, 220, 180));
            int msgWidth = g.getFontMetrics().stringWidth(statusMessage);
            g.drawString(statusMessage, (width - msgWidth) / 2, height - 30);
        }
    }

    private void adjustCurrentOption(int direction) {
        Option option = Option.values()[selectedIndex];
        switch (option) {
            case MUSIC_VOLUME:
                changeVolume(direction * 5);
                break;
            case LANGUAGE:
                changeLanguage(direction);
                break;
            default:
                break;
        }
    }

    private void handleSelection() {
        Option option = Option.values()[selectedIndex];
        if (option == Option.BACK) {
            context.getScenes().switchTo(ArkanoidGame.SCENE_MENU);
        }
    }

    private void changeVolume(int delta) {
        int next = Math.max(0, Math.min(100, musicVolume + delta));
        if (next != musicVolume) {
            musicVolume = next;
            settings.setMusicVolume(musicVolume);
            float normalized = musicVolume / 100f;
            BackgroundMusicManager.getInstance().setVolume(normalized);
            StageMusicManager.getInstance().setVolume(normalized);
            context.getSound().setGlobalVolume(normalized);
            statusMessage = localization.translate("settings.status.volume", musicVolume);
            statusTimer = STATUS_DURATION;
        }
    }

    private void changeLanguage(int direction) {
        int next = (languageIndex + direction + languages.size()) % languages.size();
        if (next != languageIndex) {
            languageIndex = next;
            LanguageOption option = languages.get(languageIndex);
            localization.setLocale(option.locale);
            statusMessage = localization.translate("settings.status.language",
                    localization.translate(option.labelKey));
            statusTimer = STATUS_DURATION;
        }
    }

    private int resolveLanguageIndex(Locale current) {
        if (current == null) {
            return 0;
        }
        for (int i = 0; i < languages.size(); i++) {
            if (languages.get(i).locale.getLanguage().equalsIgnoreCase(current.getLanguage())) {
                return i;
            }
        }
        return 0;
    }
}
