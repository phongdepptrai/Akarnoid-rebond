package com.arcade.arkanoid.gameplay.objectives;

import com.arcade.arkanoid.gameplay.levels.LevelSchema;

import java.util.List;
import java.util.Objects;

/**
 * Coordinates progress tracking for level objectives.
 */
public interface ObjectiveEngine {

    /**
     * Prepares the engine to evaluate objectives for the supplied level.
     */
    void bind(LevelSchema levelSchema, Listener listener);

    /**
     * Clears any runtime state without detaching the bound level.
     */
    void resetProgress();

    /**
     * Advances objective state using a discrete gameplay event.
     */
    void handleEvent(ObjectiveEvent event);

    /**
     * Advances timers or other continuous objectives.
     */
    void update(double deltaSeconds);

    /**
     * Returns true when all non-optional objectives have been completed and none have failed.
     */
    boolean arePrimaryObjectivesMet();

    /**
     * Returns a snapshot of every objective's current state for UI rendering.
     */
    List<ObjectiveState> snapshot();

    interface Listener {
        void onObjectiveProgress(ObjectiveState state);

        void onObjectiveCompleted(ObjectiveState state);

        void onObjectiveFailed(ObjectiveState state);
    }

    final class ScoreAwardedEvent implements ObjectiveEvent {
        public static final String TYPE = "score-awarded";

        private final int points;

        public ScoreAwardedEvent(int points) {
            if (points < 0) {
                throw new IllegalArgumentException("points must be >= 0");
            }
            this.points = points;
        }

        @Override
        public String type() {
            return TYPE;
        }

        public int points() {
            return points;
        }
    }

    interface ObjectiveEvent {
        String type();
    }

    final class BrickClearedEvent implements ObjectiveEvent {
        public static final String TYPE = "brick-cleared";

        private final int column;
        private final int row;
        private final String brickType;
        private final List<String> tags;

        public BrickClearedEvent(int column, int row, String brickType, List<String> tags) {
            if (column < 0) {
                throw new IllegalArgumentException("column must be >= 0");
            }
            if (row < 0) {
                throw new IllegalArgumentException("row must be >= 0");
            }
            this.column = column;
            this.row = row;
            this.brickType = Objects.requireNonNull(brickType, "brickType");
            this.tags = tags == null ? List.of() : List.copyOf(tags);
        }

        @Override
        public String type() {
            return TYPE;
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

        public List<String> tags() {
            return tags;
        }
    }

    final class CollectibleDeliveredEvent implements ObjectiveEvent {
        public static final String TYPE = "collectible-delivered";

        private final String collectibleId;
        private final String sourceTag;

        public CollectibleDeliveredEvent(String collectibleId, String sourceTag) {
            this.collectibleId = Objects.requireNonNull(collectibleId, "collectibleId");
            this.sourceTag = sourceTag;
        }

        @Override
        public String type() {
            return TYPE;
        }

        public String collectibleId() {
            return collectibleId;
        }

        public String sourceTag() {
            return sourceTag;
        }
    }

    final class MoveConsumedEvent implements ObjectiveEvent {
        public static final String TYPE = "move-consumed";

        @Override
        public String type() {
            return TYPE;
        }
    }

    final class TimerExpiredEvent implements ObjectiveEvent {
        public static final String TYPE = "timer-expired";

        @Override
        public String type() {
            return TYPE;
        }
    }

    final class ObjectiveState {
        private final String id;
        private final boolean optional;
        private final Status status;
        private final int progress;
        private final int target;
        private final double ratio;
        private final List<String> tags;

        public ObjectiveState(String id, boolean optional, Status status, int progress, int target, double ratio, List<String> tags) {
            this.id = Objects.requireNonNull(id, "id");
            this.optional = optional;
            this.status = Objects.requireNonNull(status, "status");
            if (progress < 0) {
                throw new IllegalArgumentException("progress must be >= 0");
            }
            this.progress = progress;
            if (target < 0) {
                throw new IllegalArgumentException("target must be >= 0");
            }
            this.target = target;
            this.ratio = ratio;
            this.tags = tags == null ? List.of() : List.copyOf(tags);
        }

        public String id() {
            return id;
        }

        public boolean optional() {
            return optional;
        }

        public Status status() {
            return status;
        }

        public int progress() {
            return progress;
        }

        public int target() {
            return target;
        }

        public double ratio() {
            return ratio;
        }

        public List<String> tags() {
            return tags;
        }
    }

    enum Status {
        IN_PROGRESS,
        COMPLETED,
        FAILED
    }
}
