package com.arcade.arkanoid.engine.settings;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

public class SettingsManager {
    private final SettingsStorage storage;
    private GameSettings settings;

    public SettingsManager() {
        this(defaultSettingsPath());
    }

    public SettingsManager(Path path) {
        this.storage = new SettingsStorage(path);
        this.settings = storage.loadOrDefault();
    }

    private static Path defaultSettingsPath() {
        String userHome = System.getProperty("user.home", ".");
        return Paths.get(userHome, ".arkanoid", "settings.json");
    }

    public GameSettings getSettings() {
        return settings;
    }

    public void save() {
        storage.save(settings);
    }

    public void toggleSound() {
        settings.setSoundEnabled(!settings.isSoundEnabled());
        save();
    }

    public void toggleMusic() {
        settings.setMusicEnabled(!settings.isMusicEnabled());
        save();
    }

    public Locale resolveLocale() {
        String localeTag = settings.getLocale();
        if (localeTag == null || localeTag.isBlank()) {
            return Locale.getDefault();
        }
        return Locale.forLanguageTag(localeTag);
    }

    public void setLocale(Locale locale) {
        if (locale == null) {
            return;
        }
        settings.setLocale(locale.toLanguageTag());
        save();
    }
}
