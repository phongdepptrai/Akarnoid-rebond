package com.arcade.arkanoid.gameplay.levels;

public class LevelDefinition {
    private final String name;
    private final int[][] layout;

    public LevelDefinition(String name, int[][] layout) {
        this.name = name;
        this.layout = layout;
    }

    public String getName() {
        return name;
    }

    public int getRows() {
        return layout.length;
    }

    public int getCols() {
        return layout.length == 0 ? 0 : layout[0].length;
    }

    public int valueAt(int row, int col) {
        return layout[row][col];
    }
}
