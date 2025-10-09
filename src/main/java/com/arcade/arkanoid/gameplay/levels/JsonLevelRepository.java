package com.arcade.arkanoid.gameplay.levels;

import com.arcade.arkanoid.gameplay.levels.LevelSchema.BoosterSlot;
import com.arcade.arkanoid.gameplay.levels.LevelSchema.BrickBlueprint;
import com.arcade.arkanoid.gameplay.levels.LevelSchema.Constraints;
import com.arcade.arkanoid.gameplay.levels.LevelSchema.Difficulty;
import com.arcade.arkanoid.gameplay.levels.LevelSchema.ObjectiveDefinition;
import com.arcade.arkanoid.gameplay.levels.LevelSchema.Reward;
import com.arcade.arkanoid.gameplay.levels.LevelSchema.RewardTier;
import com.arcade.arkanoid.gameplay.levels.LevelSchema.Target;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Loads level definitions from JSON resources located under {@code levels/}.
 */
public class JsonLevelRepository {
    private static final String MANIFEST_PATH = "levels/manifest.json";
    private final ObjectMapper mapper;

    public JsonLevelRepository() {
        mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public List<LevelDefinition> loadAll() {
        List<String> manifestEntries = readManifest();
        if (manifestEntries.isEmpty()) {
            throw new IllegalStateException("Level manifest is empty");
        }
        List<LevelDefinition> definitions = new ArrayList<>();
        for (String entry : manifestEntries) {
            LevelFile levelFile = readLevel(entry);
            definitions.add(levelFile.toDefinition());
        }
        return definitions;
    }

    private List<String> readManifest() {
        try (InputStream stream = resourceAsStream(MANIFEST_PATH)) {
            if (stream == null) {
                throw new IllegalStateException("Missing level manifest at " + MANIFEST_PATH);
            }
            Manifest manifest = mapper.readValue(stream, Manifest.class);
            if (manifest.levels == null) {
                return List.of();
            }
            return manifest.levels.stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load level manifest", e);
        }
    }

    private LevelFile readLevel(String fileName) {
        String path = "levels/" + fileName;
        try (InputStream stream = resourceAsStream(path)) {
            if (stream == null) {
                throw new IllegalStateException("Missing level file: " + path);
            }
            return mapper.readValue(stream, LevelFile.class);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load level file " + path, e);
        }
    }

    private InputStream resourceAsStream(String path) {
        return Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(path);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Manifest {
        public List<String> levels;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class LevelFile {
        public String id;
        public String displayName;
        public String description;
        public Difficulty difficulty;
        public Board board;
        public FileConstraints constraints;
        public List<FileBrick> bricks;
        public List<FileObjective> objectives;
        public List<FileBooster> boosters;
        public List<FileRewardTier> rewardTiers;

        LevelDefinition toDefinition() {
            if (id == null || id.isEmpty()) {
                throw new IllegalStateException("Level id must be provided");
            }
            if (board == null) {
                throw new IllegalStateException("Level board must be provided");
            }
            Difficulty resolvedDifficulty = difficulty == null ? Difficulty.STORY : difficulty;
            Constraints resolvedConstraints = constraints == null
                    ? new Constraints(null, null)
                    : constraints.toConstraints();

            List<BrickBlueprint> brickBlueprints = bricks == null
                    ? List.of()
                    : bricks.stream()
                    .map(FileBrick::toBlueprint)
                    .collect(Collectors.toList());

            List<ObjectiveDefinition> objectiveDefinitions = objectives == null
                    ? List.of()
                    : objectives.stream()
                    .map(FileObjective::toObjective)
                    .collect(Collectors.toList());

            List<BoosterSlot> boosterSlots = boosters == null
                    ? List.of()
                    : boosters.stream()
                    .map(FileBooster::toBoosterSlot)
                    .collect(Collectors.toList());

            List<RewardTier> rewardTierList = rewardTiers == null
                    ? List.of()
                    : rewardTiers.stream()
                    .map(FileRewardTier::toRewardTier)
                    .collect(Collectors.toList());

            return new LevelDefinition(
                    id,
                    displayName == null || displayName.isEmpty() ? id : displayName,
                    description,
                    resolvedDifficulty,
                    new LevelSchema.Board(board.columns, board.rows),
                    resolvedConstraints,
                    brickBlueprints,
                    objectiveDefinitions,
                    boosterSlots,
                    rewardTierList
            );
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Board {
        public int columns;
        public int rows;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class FileConstraints {
        public Integer moveLimit;
        public Double timeLimitSeconds;

        Constraints toConstraints() {
            return new Constraints(moveLimit, timeLimitSeconds);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class FileBrick {
        public int column;
        public int row;
        public String brickType;
        public Integer hitPoints;
        public List<String> tags;
        public List<String> modifiers;

        BrickBlueprint toBlueprint() {
            int resolvedHitPoints = hitPoints == null ? 1 : hitPoints;
            return new BrickBlueprint(
                    column,
                    row,
                    brickType == null ? "basic" : brickType,
                    resolvedHitPoints,
                    tags,
                    modifiers
            );
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class FileObjective {
        public String id;
        public String type;
        public FileTarget target;
        public Boolean optional;
        public List<String> tags;

        ObjectiveDefinition toObjective() {
            if (id == null || id.isEmpty()) {
                throw new IllegalStateException("Objective id is required");
            }
            if (type == null || type.isEmpty()) {
                throw new IllegalStateException("Objective type is required");
            }
            Target resolvedTarget = target == null ? new Target(0, null, List.of()) : target.toTarget();
            return new ObjectiveDefinition(
                    id,
                    type,
                    resolvedTarget,
                    optional != null && optional,
                    tags
            );
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class FileTarget {
        public Integer amount;
        public Double timeLimitSeconds;
        public List<String> trackedTags;

        Target toTarget() {
            int resolvedAmount = amount == null ? 0 : amount;
            return new Target(resolvedAmount, timeLimitSeconds, trackedTags);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class FileBooster {
        public String boosterId;
        public Integer quantity;

        BoosterSlot toBoosterSlot() {
            return new BoosterSlot(
                    boosterId == null ? "booster:unknown" : boosterId,
                    quantity == null ? 0 : quantity
            );
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class FileRewardTier {
        public Integer stars;
        public List<FileReward> rewards;

        RewardTier toRewardTier() {
            List<Reward> rewardList = rewards == null
                    ? List.of()
                    : rewards.stream()
                    .map(FileReward::toReward)
                    .collect(Collectors.toList());
            return new RewardTier(stars == null ? 0 : stars, rewardList);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class FileReward {
        public String type;
        public Integer quantity;

        Reward toReward() {
            return new Reward(type == null ? "currency:coins" : type, quantity == null ? 0 : quantity);
        }
    }
}
