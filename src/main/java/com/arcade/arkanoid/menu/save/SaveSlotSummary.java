package com.arcade.arkanoid.menu.save;

import com.arcade.arkanoid.profile.PlayerProfile;

public class SaveSlotSummary {
    private final int slotId;
    private final String displayName;
    private final String currentLevelId;
    private final int lives;
    private final int coins;
    private final int dailyStreak;
    private final long lastPlayedEpochSeconds;
    private final boolean occupied;
    private final PlayerProfile profile;

    public SaveSlotSummary(int slotId, PlayerProfile profile, long lastPlayedEpochSeconds) {
        this.slotId = slotId;
        this.profile = profile;
        if (profile == null) {
            this.displayName = "Empty Slot";
            this.currentLevelId = "---";
            this.lives = 0;
            this.coins = 0;
            this.dailyStreak = 0;
            this.lastPlayedEpochSeconds = 0;
            this.occupied = false;
        } else {
            this.displayName = profile.getDisplayName();
            this.currentLevelId = profile.getCurrentLevelId();
            this.lives = profile.getLives();
            this.coins = profile.getCoins();
            this.dailyStreak = profile.getDailyStreak();
            this.lastPlayedEpochSeconds = lastPlayedEpochSeconds;
            this.occupied = true;
        }
    }

    public int getSlotId() {
        return slotId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCurrentLevelId() {
        return currentLevelId;
    }

    public int getLives() {
        return lives;
    }

    public int getCoins() {
        return coins;
    }

    public int getDailyStreak() {
        return dailyStreak;
    }

    public long getLastPlayedEpochSeconds() {
        return lastPlayedEpochSeconds;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public PlayerProfile getProfile() {
        return profile;
    }
}
