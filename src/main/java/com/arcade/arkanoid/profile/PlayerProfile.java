package com.arcade.arkanoid.profile;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Data container describing the player's persistent progress, lives, and currencies.
 */
public class PlayerProfile {
    private String playerId;
    private String displayName;
    private String currentLevelId;
    private List<String> unlockedLevelIds;
    private List<String> completedLevelIds;
    private int lives;
    private int maxLives;
    private int coins;
    private int energy;
    private int maxEnergy;
    private long lastLoginEpochSeconds;
    private int dailyStreak;
    private long lastDailyBonusEpochSeconds;

    public PlayerProfile() {
        // Jackson / serialization constructor.
    }

    private PlayerProfile(String playerId,
                          String displayName,
                          String currentLevelId,
                          List<String> unlockedLevelIds,
                          List<String> completedLevelIds,
                          int lives,
                          int maxLives,
                          int coins,
                          int energy,
                          int maxEnergy,
                          long lastLoginEpochSeconds,
                          int dailyStreak,
                          long lastDailyBonusEpochSeconds) {
        this.playerId = playerId;
        this.displayName = displayName;
        this.currentLevelId = currentLevelId;
        this.unlockedLevelIds = unlockedLevelIds;
        this.completedLevelIds = completedLevelIds;
        this.lives = lives;
        this.maxLives = maxLives;
        this.coins = coins;
        this.energy = energy;
        this.maxEnergy = maxEnergy;
        this.lastLoginEpochSeconds = lastLoginEpochSeconds;
        this.dailyStreak = dailyStreak;
        this.lastDailyBonusEpochSeconds = lastDailyBonusEpochSeconds;
    }

    public static PlayerProfile newDefault() {
        long now = Instant.now().getEpochSecond();
        List<String> unlocked = new ArrayList<>();
        unlocked.add("001");
        List<String> completed = new ArrayList<>();
        return new PlayerProfile(
                UUID.randomUUID().toString(),
                "Player",
                "001",
                unlocked,
                completed,
                5,
                5,
                0,
                30,
                30,
                now,
                0,
                0
        );
    }

    public void ensureDefaults() {
        if (playerId == null || playerId.isBlank()) {
            playerId = UUID.randomUUID().toString();
        }
        if (displayName == null || displayName.isBlank()) {
            displayName = "Player";
        }
        if (currentLevelId == null || currentLevelId.isBlank()) {
            currentLevelId = "001";
        }
        if (unlockedLevelIds == null) {
            unlockedLevelIds = new ArrayList<>();
        }
        if (!unlockedLevelIds.contains(currentLevelId)) {
            unlockedLevelIds.add(currentLevelId);
        }
        if (completedLevelIds == null) {
            completedLevelIds = new ArrayList<>();
        }
        maxLives = Math.max(maxLives, 5);
        lives = Math.min(Math.max(lives, 0), maxLives);
        maxEnergy = Math.max(maxEnergy, 30);
        energy = Math.min(Math.max(energy, 0), maxEnergy);
        if (lastLoginEpochSeconds <= 0) {
            lastLoginEpochSeconds = Instant.now().getEpochSecond();
        }
        if (dailyStreak < 0) {
            dailyStreak = 0;
        }
    }

    public boolean isLevelUnlocked(String levelId) {
        if (levelId == null || levelId.isBlank()) {
            return false;
        }
        return unlockedLevelIds != null && unlockedLevelIds.contains(levelId);
    }

    public void unlockLevel(String levelId) {
        if (levelId == null || levelId.isBlank()) {
            return;
        }
        if (unlockedLevelIds == null) {
            unlockedLevelIds = new ArrayList<>();
        }
        if (!unlockedLevelIds.contains(levelId)) {
            unlockedLevelIds.add(levelId);
        }
    }

    public boolean isLevelCompleted(String levelId) {
        if (levelId == null || levelId.isBlank()) {
            return false;
        }
        return completedLevelIds != null && completedLevelIds.contains(levelId);
    }

    public void markLevelCompleted(String levelId) {
        if (levelId == null || levelId.isBlank()) {
            return;
        }
        if (completedLevelIds == null) {
            completedLevelIds = new ArrayList<>();
        }
        if (!completedLevelIds.contains(levelId)) {
            completedLevelIds.add(levelId);
        }
    }

    public List<String> getCompletedLevelIds() {
        if (completedLevelIds == null) {
            completedLevelIds = new ArrayList<>();
        }
        return completedLevelIds;
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = Objects.requireNonNull(displayName);
    }

    public String getCurrentLevelId() {
        return currentLevelId;
    }

    public void setCurrentLevelId(String currentLevelId) {
        this.currentLevelId = currentLevelId;
    }

    public List<String> getUnlockedLevelIds() {
        if (unlockedLevelIds == null) {
            unlockedLevelIds = new ArrayList<>();
        }
        return unlockedLevelIds;
    }

    public int getLives() {
        return lives;
    }

    public void setLives(int lives) {
        this.lives = lives;
    }

    public int getMaxLives() {
        return maxLives;
    }

    public void setMaxLives(int maxLives) {
        this.maxLives = maxLives;
    }

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public int getEnergy() {
        return energy;
    }

    public void setEnergy(int energy) {
        this.energy = energy;
    }

    public int getMaxEnergy() {
        return maxEnergy;
    }

    public void setMaxEnergy(int maxEnergy) {
        this.maxEnergy = maxEnergy;
    }

    public long getLastLoginEpochSeconds() {
        return lastLoginEpochSeconds;
    }

    public void setLastLoginEpochSeconds(long lastLoginEpochSeconds) {
        this.lastLoginEpochSeconds = lastLoginEpochSeconds;
    }

    public int getDailyStreak() {
        return dailyStreak;
    }

    public void setDailyStreak(int dailyStreak) {
        this.dailyStreak = dailyStreak;
    }

    public long getLastDailyBonusEpochSeconds() {
        return lastDailyBonusEpochSeconds;
    }

    public void setLastDailyBonusEpochSeconds(long lastDailyBonusEpochSeconds) {
        this.lastDailyBonusEpochSeconds = lastDailyBonusEpochSeconds;
    }
}
