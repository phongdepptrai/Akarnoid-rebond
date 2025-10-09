package com.arcade.arkanoid.gameplay.objectives;

import com.arcade.arkanoid.gameplay.levels.LevelSchema;
import com.arcade.arkanoid.gameplay.objectives.ObjectiveEngine.Status;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class StandardObjectiveEngine implements ObjectiveEngine {
    private static final Listener NO_OP_LISTENER = new Listener() {
        @Override
        public void onObjectiveProgress(ObjectiveState state) {
        }

        @Override
        public void onObjectiveCompleted(ObjectiveState state) {
        }

        @Override
        public void onObjectiveFailed(ObjectiveState state) {
        }
    };

    private final Map<String, Tracker> trackers = new LinkedHashMap<>();
    private LevelSchema levelSchema;
    private Listener listener = NO_OP_LISTENER;

    @Override
    public void bind(LevelSchema levelSchema, Listener listener) {
        this.levelSchema = levelSchema;
        this.listener = listener == null ? NO_OP_LISTENER : listener;
        rebuildTrackers();
    }

    @Override
    public void resetProgress() {
        trackers.values().forEach(Tracker::reset);
    }

    @Override
    public void handleEvent(ObjectiveEvent event) {
        if (event == null) {
            return;
        }
        if (event instanceof BrickClearedEvent) {
            handleBrickCleared((BrickClearedEvent) event);
        } else if (event instanceof ScoreAwardedEvent) {
            handleScoreAwarded((ScoreAwardedEvent) event);
        } else if (event instanceof TimerExpiredEvent) {
            handleTimerExpired();
        } else if (event instanceof MoveConsumedEvent) {
            handleMoveConsumed();
        } else if (event instanceof CollectibleDeliveredEvent) {
            handleCollectibleDelivered((CollectibleDeliveredEvent) event);
        }
    }

    @Override
    public void update(double deltaSeconds) {
        for (Tracker tracker : trackers.values()) {
            tracker.update(deltaSeconds, listener);
        }
    }

    @Override
    public boolean arePrimaryObjectivesMet() {
        if (trackers.isEmpty()) {
            return false;
        }
        return trackers.values().stream()
                .filter(tracker -> !tracker.definition.optional())
                .allMatch(tracker -> tracker.status == Status.COMPLETED);
    }

    @Override
    public List<ObjectiveState> snapshot() {
        return trackers.values()
                .stream()
                .map(Tracker::toState)
                .collect(Collectors.toList());
    }

    private void rebuildTrackers() {
        trackers.clear();
        if (levelSchema == null) {
            return;
        }
        for (LevelSchema.ObjectiveDefinition definition : levelSchema.objectives()) {
            Tracker tracker = new Tracker(definition);
            trackers.put(definition.id(), tracker);
        }
    }

    private void handleBrickCleared(BrickClearedEvent event) {
        for (Tracker tracker : trackers.values()) {
            if (!tracker.isActive()) {
                continue;
            }
            if (ObjectiveTypes.CLEAR_TAGGED_BRICKS.equals(tracker.type())) {
                if (matchesAnyTag(tracker.trackedTags(), event.tags())) {
                    tracker.increment(1, listener);
                }
            }
        }
    }

    private void handleScoreAwarded(ScoreAwardedEvent event) {
        for (Tracker tracker : trackers.values()) {
            if (!tracker.isActive()) {
                continue;
            }
            if (ObjectiveTypes.SCORE.equals(tracker.type())) {
                tracker.increment(event.points(), listener);
            }
        }
    }

    private void handleCollectibleDelivered(CollectibleDeliveredEvent event) {
        List<String> eventTags = new ArrayList<>();
        if (event.collectibleId() != null) {
            eventTags.add(event.collectibleId());
        }
        if (event.sourceTag() != null && !event.sourceTag().isEmpty()) {
            eventTags.add(event.sourceTag());
        }
        for (Tracker tracker : trackers.values()) {
            if (!tracker.isActive()) {
                continue;
            }
            if (ObjectiveTypes.DELIVER_COLLECTIBLES.equals(tracker.type())) {
                if (matchesAnyTag(tracker.trackedTags(), eventTags)) {
                    tracker.increment(1, listener);
                }
            }
        }
    }

    private void handleMoveConsumed() {
        for (Tracker tracker : trackers.values()) {
            tracker.onMoveConsumed(listener);
        }
    }

    private void handleTimerExpired() {
        for (Tracker tracker : trackers.values()) {
            tracker.failDueToTimer(listener);
        }
    }

    private boolean matchesAnyTag(List<String> trackedTags, List<String> eventTags) {
        if (trackedTags == null || trackedTags.isEmpty()) {
            return true;
        }
        if (eventTags == null || eventTags.isEmpty()) {
            return false;
        }
        for (String tag : trackedTags) {
            if (tag == null || tag.isEmpty()) {
                continue;
            }
            for (String eventTag : eventTags) {
                if (tag.equals(eventTag)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static class Tracker {
        private final LevelSchema.ObjectiveDefinition definition;
        private final int target;
        private final List<String> trackedTags;
        private final double initialTimeLimit;
        private int progress;
        private Status status;
        private double timeRemaining;

        Tracker(LevelSchema.ObjectiveDefinition definition) {
            this.definition = Objects.requireNonNull(definition, "definition");
            this.target = Math.max(0, definition.target().amount());
            this.trackedTags = definition.target().trackedTags();
            this.initialTimeLimit = definition.target().hasTimeLimit()
                    ? definition.target().timeLimitSeconds()
                    : -1.0;
            reset();
        }

        void reset() {
            this.progress = 0;
            if (target == 0) {
                status = Status.COMPLETED;
            } else {
                status = Status.IN_PROGRESS;
            }
            this.timeRemaining = initialTimeLimit;
        }

        boolean isActive() {
            return status == Status.IN_PROGRESS;
        }

        String type() {
            return definition.type();
        }

        List<String> trackedTags() {
            return trackedTags;
        }

        void increment(int amount, Listener listener) {
            if (!isActive() || amount <= 0) {
                return;
            }
            int previous = progress;
            progress = Math.min(progress + amount, target);
            listener.onObjectiveProgress(toState());
            if (progress >= target && previous < target) {
                status = Status.COMPLETED;
                listener.onObjectiveCompleted(toState());
            }
        }

        void update(double deltaSeconds, Listener listener) {
            if (!isActive() || initialTimeLimit <= 0) {
                return;
            }
            timeRemaining -= deltaSeconds;
            if (timeRemaining <= 0) {
                status = Status.FAILED;
                listener.onObjectiveFailed(toState());
            }
        }

        void failDueToTimer(Listener listener) {
            if (!isActive()) {
                return;
            }
            status = Status.FAILED;
            listener.onObjectiveFailed(toState());
        }

        void onMoveConsumed(Listener listener) {
            if (!isActive()) {
                return;
            }
            if (ObjectiveTypes.MOVE_LIMIT.equals(type())) {
                increment(1, listener);
            }
        }

        ObjectiveState toState() {
            double ratio;
            if (target > 0) {
                ratio = Math.min(1.0, (double) progress / target);
            } else {
                ratio = status == Status.COMPLETED ? 1.0 : 0.0;
            }
            return new ObjectiveState(
                    definition.id(),
                    definition.optional(),
                    status,
                    progress,
                    target,
                    ratio,
                    definition.tags()
            );
        }
    }

    private static final class ObjectiveTypes {
        private static final String CLEAR_TAGGED_BRICKS = "clear-tagged-bricks";
        private static final String DELIVER_COLLECTIBLES = "deliver-collectibles";
        private static final String SCORE = "score";
        private static final String MOVE_LIMIT = "move-limit";

        private ObjectiveTypes() {
        }
    }
}
