package com.arcade.arkanoid.gameplay.levels;

import java.util.ArrayList;
import java.util.List;

public class LevelManager {
    private final List<LevelDefinition> levels = new ArrayList<>();
    private int currentIndex;

    public LevelManager() {
        levels.add(new LevelDefinition("Training Grounds", parse(
                "1111111111",
                "1000000001",
                "1111111111"
        )));
        levels.add(new LevelDefinition("Steel Curtain", parse(
                "2222222222",
                "2111111112",
                "2000000002",
                "2111111112",
                "2222222222"
        )));
        levels.add(new LevelDefinition("Diamond Maze", parse(
                "0033333300",
                "0333333330",
                "3332223333",
                "0333333330",
                "0033333300"
        )));
    }

    private int[][] parse(String... rows) {
        int[][] layout = new int[rows.length][rows[0].length()];
        for (int row = 0; row < rows.length; row++) {
            char[] chars = rows[row].toCharArray();
            for (int col = 0; col < chars.length; col++) {
                layout[row][col] = Character.digit(chars[col], 10);
            }
        }
        return layout;
    }

    public LevelDefinition current() {
        return levels.get(currentIndex);
    }

    public boolean hasNext() {
        return currentIndex < levels.size() - 1;
    }

    public void advance() {
        if (hasNext()) {
            currentIndex++;
        }
    }

    public void reset() {
        currentIndex = 0;
    }
}
