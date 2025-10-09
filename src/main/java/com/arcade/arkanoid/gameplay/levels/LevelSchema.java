package com.arcade.arkanoid.gameplay.levels;

import java.util.List;
import java.util.Objects;

/**
 * Declarative description of a level that can be loaded from external data.
 * Implementations are expected to be immutable value objects that mirror the JSON schema.
 */
public interface LevelSchema {

    String id();

    String displayName();

    String description();

    Difficulty difficulty();

    Board board();

    Constraints constraints();

    List<BrickBlueprint> bricks();

    List<ObjectiveDefinition> objectives();

    List<BoosterSlot> boosters();

    List<RewardTier> rewardTiers();

    enum Difficulty {
        STORY,
        CHALLENGE,
        BOSS
    }

    final class Board {
        private final int columns;
        private final int rows;

        public Board(int columns, int rows) {
            if (columns <= 0) {
                throw new IllegalArgumentException("columns must be positive");
            }
            if (rows <= 0) {
                throw new IllegalArgumentException("rows must be positive");
            }
            this.columns = columns;
            this.rows = rows;
        }

        public int columns() {
            return columns;
        }

        public int rows() {
            return rows;
        }
    }

    final class Constraints {
        private final Integer moveLimit;
        private final Double timeLimitSeconds;

        public Constraints(Integer moveLimit, Double timeLimitSeconds) {
            this.moveLimit = moveLimit;
            this.timeLimitSeconds = timeLimitSeconds;
        }

        public Integer moveLimit() {
            return moveLimit;
        }

        public Double timeLimitSeconds() {
            return timeLimitSeconds;
        }

        public boolean hasMoveLimit() {
            return moveLimit != null;
        }

        public boolean hasTimeLimit() {
            return timeLimitSeconds != null;
        }
    }

    final class BrickBlueprint {
        private final int column;
        private final int row;
        private final String brickType;
        private final int hitPoints;
        private final List<String> tags;
        private final List<String> modifiers;

        public BrickBlueprint(int column, int row, String brickType, int hitPoints, List<String> tags, List<String> modifiers) {
            if (column < 0) {
                throw new IllegalArgumentException("column must be >= 0");
            }
            if (row < 0) {
                throw new IllegalArgumentException("row must be >= 0");
            }
            this.column = column;
            this.row = row;
            this.brickType = Objects.requireNonNull(brickType, "brickType");
            this.hitPoints = Math.max(hitPoints, 1);
            this.tags = tags == null ? List.of() : List.copyOf(tags);
            this.modifiers = modifiers == null ? List.of() : List.copyOf(modifiers);
        }

        public int column() {
            return column;
        }

        public int row() {
            return row;
        }

        public String brickType() {
            return brickType;
        }

        public int hitPoints() {
            return hitPoints;
        }

        public List<String> tags() {
            return tags;
        }

        public List<String> modifiers() {
            return modifiers;
        }
    }

    final class ObjectiveDefinition {
        private final String id;
        private final String type;
        private final Target target;
        private final boolean optional;
        private final List<String> tags;

        public ObjectiveDefinition(String id, String type, Target target, boolean optional, List<String> tags) {
            this.id = Objects.requireNonNull(id, "id");
            this.type = Objects.requireNonNull(type, "type");
            this.target = Objects.requireNonNull(target, "target");
            this.optional = optional;
            this.tags = tags == null ? List.of() : List.copyOf(tags);
        }

        public String id() {
            return id;
        }

        public String type() {
            return type;
        }

        public Target target() {
            return target;
        }

        public boolean optional() {
            return optional;
        }

        public List<String> tags() {
            return tags;
        }
    }

    final class Target {
        private final int amount;
        private final Double timeLimitSeconds;
        private final List<String> trackedTags;

        public Target(int amount, Double timeLimitSeconds, List<String> trackedTags) {
            if (amount < 0) {
                throw new IllegalArgumentException("amount must be >= 0");
            }
            this.amount = amount;
            this.timeLimitSeconds = timeLimitSeconds;
            this.trackedTags = trackedTags == null ? List.of() : List.copyOf(trackedTags);
        }

        public int amount() {
            return amount;
        }

        public Double timeLimitSeconds() {
            return timeLimitSeconds;
        }

        public List<String> trackedTags() {
            return trackedTags;
        }

        public boolean hasTimeLimit() {
            return timeLimitSeconds != null;
        }
    }

    final class BoosterSlot {
        private final String boosterId;
        private final int quantity;

        public BoosterSlot(String boosterId, int quantity) {
            this.boosterId = Objects.requireNonNull(boosterId, "boosterId");
            if (quantity < 0) {
                throw new IllegalArgumentException("quantity must be >= 0");
            }
            this.quantity = quantity;
        }

        public String boosterId() {
            return boosterId;
        }

        public int quantity() {
            return quantity;
        }
    }

    final class RewardTier {
        private final int stars;
        private final List<Reward> rewards;

        public RewardTier(int stars, List<Reward> rewards) {
            if (stars < 0) {
                throw new IllegalArgumentException("stars must be >= 0");
            }
            this.stars = stars;
            this.rewards = rewards == null ? List.of() : List.copyOf(rewards);
        }

        public int stars() {
            return stars;
        }

        public List<Reward> rewards() {
            return rewards;
        }
    }

    final class Reward {
        private final String type;
        private final int quantity;

        public Reward(String type, int quantity) {
            this.type = Objects.requireNonNull(type, "type");
            this.quantity = quantity;
        }

        public String type() {
            return type;
        }

        public int quantity() {
            return quantity;
        }
    }
}
