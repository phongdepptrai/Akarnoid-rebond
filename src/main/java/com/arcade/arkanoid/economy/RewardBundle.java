package com.arcade.arkanoid.economy;

public class RewardBundle {
    private final int coins;
    private final int lives;
    private final int energy;

    public RewardBundle(int coins, int lives, int energy) {
        this.coins = coins;
        this.lives = lives;
        this.energy = energy;
    }

    public int getCoins() {
        return coins;
    }

    public int getLives() {
        return lives;
    }

    public int getEnergy() {
        return energy;
    }
}
