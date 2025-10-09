package com.arcade.arkanoid.economy;

public class DailyBonusResult {
    private final boolean granted;
    private final int streak;
    private final int coinsAwarded;
    private final int livesAwarded;

    public DailyBonusResult(boolean granted, int streak, int coinsAwarded, int livesAwarded) {
        this.granted = granted;
        this.streak = streak;
        this.coinsAwarded = coinsAwarded;
        this.livesAwarded = livesAwarded;
    }

    public boolean isGranted() {
        return granted;
    }

    public int getStreak() {
        return streak;
    }

    public int getCoinsAwarded() {
        return coinsAwarded;
    }

    public int getLivesAwarded() {
        return livesAwarded;
    }
}
