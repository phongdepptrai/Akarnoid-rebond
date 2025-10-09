package com.arcade.arkanoid.economy;

import com.arcade.arkanoid.profile.PlayerProfile;
import com.arcade.arkanoid.profile.ProfileManager;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

/**
 * Coordinates currencies, lives/energy, and daily bonus claims.
 */
public class EconomyService {
    private static final int DEFAULT_DAILY_COIN_REWARD = 50;
    private static final int DEFAULT_DAILY_LIFE_REWARD = 1;

    private final ProfileManager profileManager;

    public EconomyService(ProfileManager profileManager) {
        this.profileManager = profileManager;
    }

    public boolean spendCoins(int amount) {
        if (amount <= 0) {
            return true;
        }
        PlayerProfile profile = profileManager.getActiveProfile();
        if (profile.getCoins() < amount) {
            return false;
        }
        profile.setCoins(profile.getCoins() - amount);
        profileManager.saveProfile();
        return true;
    }

    public void addCoins(int amount) {
        if (amount <= 0) {
            return;
        }
        PlayerProfile profile = profileManager.getActiveProfile();
        profile.setCoins(Math.max(0, profile.getCoins() + amount));
        profileManager.saveProfile();
    }

    public boolean consumeLife() {
        PlayerProfile profile = profileManager.getActiveProfile();
        if (profile.getLives() <= 0) {
            return false;
        }
        profile.setLives(profile.getLives() - 1);
        profileManager.saveProfile();
        return true;
    }

    public void awardLife(int count) {
        if (count <= 0) {
            return;
        }
        PlayerProfile profile = profileManager.getActiveProfile();
        int newLives = Math.min(profile.getMaxLives(), profile.getLives() + count);
        profile.setLives(newLives);
        profileManager.saveProfile();
    }

    public void refillLives() {
        PlayerProfile profile = profileManager.getActiveProfile();
        profile.setLives(profile.getMaxLives());
        profileManager.saveProfile();
    }

    public void awardEnergy(int amount) {
        if (amount <= 0) {
            return;
        }
        PlayerProfile profile = profileManager.getActiveProfile();
        int newEnergy = Math.min(profile.getMaxEnergy(), profile.getEnergy() + amount);
        profile.setEnergy(newEnergy);
        profileManager.saveProfile();
    }

    public boolean consumeEnergy(int amount) {
        if (amount <= 0) {
            return true;
        }
        PlayerProfile profile = profileManager.getActiveProfile();
        if (profile.getEnergy() < amount) {
            return false;
        }
        profile.setEnergy(profile.getEnergy() - amount);
        profileManager.saveProfile();
        return true;
    }

    public DailyBonusResult claimDailyBonus() {
        PlayerProfile profile = profileManager.getActiveProfile();
        long now = Instant.now().getEpochSecond();
        LocalDate today = Instant.ofEpochSecond(now).atZone(ZoneOffset.UTC).toLocalDate();
        LocalDate lastClaimDate = Instant.ofEpochSecond(profile.getLastDailyBonusEpochSeconds())
                .atZone(ZoneOffset.UTC)
                .toLocalDate();

        if (today.equals(lastClaimDate)) {
            return new DailyBonusResult(false, profile.getDailyStreak(), 0, 0);
        }

        if (lastClaimDate.plusDays(1).equals(today)) {
            profile.setDailyStreak(profile.getDailyStreak() + 1);
        } else {
            profile.setDailyStreak(1);
        }

        int bonusCoins = DEFAULT_DAILY_COIN_REWARD + (profile.getDailyStreak() - 1) * 10;
        int bonusLives = DEFAULT_DAILY_LIFE_REWARD;
        profile.setCoins(profile.getCoins() + bonusCoins);
        profile.setLives(Math.min(profile.getMaxLives(), profile.getLives() + bonusLives));
        profile.setLastDailyBonusEpochSeconds(now);
        profileManager.saveProfile();

        return new DailyBonusResult(true, profile.getDailyStreak(), bonusCoins, bonusLives);
    }

    public void applyReward(RewardBundle rewardBundle) {
        if (rewardBundle == null) {
            return;
        }
        if (rewardBundle.getCoins() != 0) {
            addCoins(rewardBundle.getCoins());
        }
        if (rewardBundle.getLives() > 0) {
            awardLife(rewardBundle.getLives());
        }
        if (rewardBundle.getEnergy() > 0) {
            awardEnergy(rewardBundle.getEnergy());
        }
    }
}
