package com.arcade.arkanoid.gameplay.levels;

import java.util.List;
import java.util.Objects;

public class LevelDefinition implements LevelSchema {
    private final String id;
    private final String displayName;
    private final String description;
    private final Difficulty difficulty;
    private final Board board;
    private final Constraints constraints;
    private final List<BrickBlueprint> bricks;
    private final List<ObjectiveDefinition> objectives;
    private final List<BoosterSlot> boosters;
    private final List<RewardTier> rewardTiers;

    public LevelDefinition(String id,
                           String displayName,
                           String description,
                           Difficulty difficulty,
                           Board board,
                           Constraints constraints,
                           List<BrickBlueprint> bricks,
                           List<ObjectiveDefinition> objectives,
                           List<BoosterSlot> boosters,
                           List<RewardTier> rewardTiers) {
        this.id = Objects.requireNonNull(id, "id");
        this.displayName = Objects.requireNonNull(displayName, "displayName");
        this.description = description == null ? "" : description;
        this.difficulty = Objects.requireNonNull(difficulty, "difficulty");
        this.board = Objects.requireNonNull(board, "board");
        this.constraints = constraints == null ? new Constraints(null, null) : constraints;
        this.bricks = bricks == null ? List.of() : List.copyOf(bricks);
        this.objectives = objectives == null ? List.of() : List.copyOf(objectives);
        this.boosters = boosters == null ? List.of() : List.copyOf(boosters);
        this.rewardTiers = rewardTiers == null ? List.of() : List.copyOf(rewardTiers);
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String displayName() {
        return displayName;
    }

    @Override
    public String description() {
        return description;
    }

    @Override
    public Difficulty difficulty() {
        return difficulty;
    }

    @Override
    public Board board() {
        return board;
    }

    @Override
    public Constraints constraints() {
        return constraints;
    }

    @Override
    public List<BrickBlueprint> bricks() {
        return bricks;
    }

    @Override
    public List<ObjectiveDefinition> objectives() {
        return objectives;
    }

    @Override
    public List<BoosterSlot> boosters() {
        return boosters;
    }

    @Override
    public List<RewardTier> rewardTiers() {
        return rewardTiers;
    }

    public int columns() {
        return board.columns();
    }

    public int rows() {
        return board.rows();
    }
}
