package com.arcade.arkanoid.gameplay.levels;

import java.util.List;

public class LevelManager {
    private final List<LevelDefinition> levels;
    private int currentIndex;

    public LevelManager() {
        this(new JsonLevelRepository().loadAll());
    }

    LevelManager(List<LevelDefinition> levels) {
        if (levels == null || levels.isEmpty()) {
            throw new IllegalArgumentException("At least one level definition is required");
        }
        this.levels = List.copyOf(levels);
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

    public int totalLevels() {
        return levels.size();
    }

    public boolean selectLevel(String levelId) {
        int index = findIndex(levelId);
        if (index >= 0) {
            currentIndex = index;
            return true;
        }
        return false;
    }

    public void resetToLevel(String levelId) {
        if (!selectLevel(levelId)) {
            reset();
        }
    }

    private int findIndex(String levelId) {
        if (levelId == null || levelId.isBlank()) {
            return -1;
        }
        for (int i = 0; i < levels.size(); i++) {
            if (levelId.equals(levels.get(i).id())) {
                return i;
            }
        }
        return -1;
    }
}
