package com.arcade.arkanoid.engine.settings;

import java.util.Locale;

public class GameSettings {
    private boolean soundEnabled;
    private boolean musicEnabled;
    private String locale;
    private int musicVolume;

    public GameSettings() {
        this.soundEnabled = true;
        this.musicEnabled = true;
        this.locale = Locale.getDefault().toLanguageTag();
        this.musicVolume = 100;
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    public void setSoundEnabled(boolean soundEnabled) {
        this.soundEnabled = soundEnabled;
    }

    public boolean isMusicEnabled() {
        return musicEnabled;
    }

    public void setMusicEnabled(boolean musicEnabled) {
        this.musicEnabled = musicEnabled;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public int getMusicVolume() {
        return musicVolume;
    }

    public void setMusicVolume(int musicVolume) {
        this.musicVolume = musicVolume;
    }

    public void ensureDefaults() {
        if (locale == null || locale.isBlank()) {
            locale = Locale.getDefault().toLanguageTag();
        }
        if (musicVolume < 0 || musicVolume > 100) {
            musicVolume = Math.min(100, Math.max(0, musicVolume));
        }
    }
}
