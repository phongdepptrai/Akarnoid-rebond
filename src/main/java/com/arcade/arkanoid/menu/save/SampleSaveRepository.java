package com.arcade.arkanoid.menu.save;

import com.arcade.arkanoid.profile.PlayerProfile;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class SampleSaveRepository {
    private final Path saveDir;
    private final ObjectMapper mapper;

    public SampleSaveRepository() {
        this(Paths.get("data", "profiles"));
    }

    public SampleSaveRepository(Path saveDir) {
        this.saveDir = saveDir;
        this.mapper = new ObjectMapper().findAndRegisterModules();
    }

    public List<SaveSlotSummary> loadSlots(int maxSlots) {
        List<SaveSlotSummary> summaries = new ArrayList<>();
        for (int slot = 1; slot <= maxSlots; slot++) {
            summaries.add(loadSlot(slot));
        }
        return summaries;
    }

    private SaveSlotSummary loadSlot(int slotId) {
        Path path = resolveSlotPath(slotId);
        if (!Files.exists(path)) {
            return new SaveSlotSummary(slotId, null, 0);
        }
        try {
            SlotFile file = mapper.readValue(path.toFile(), SlotFile.class);
            PlayerProfile profile = file.profile;
            if (profile != null) {
                profile.ensureDefaults();
            }
            return new SaveSlotSummary(slotId, profile, file.lastPlayed);
        } catch (IOException e) {
            System.err.println("Failed to load save slot " + slotId + ": " + e.getMessage());
            return new SaveSlotSummary(slotId, null, 0);
        }
    }

    public void deleteSlot(int slotId) {
        Path path = resolveSlotPath(slotId);
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            System.err.println("Failed to delete save slot " + slotId + ": " + e.getMessage());
        }
    }

    public void writeSlot(int slotId, PlayerProfile profile) {
        profile.ensureDefaults();
        Path path = resolveSlotPath(slotId);
        try {
            Files.createDirectories(path.getParent());
            SlotFile file = new SlotFile();
            file.slotId = slotId;
            file.name = profile.getDisplayName();
            file.profile = profile;
            file.lastPlayed = System.currentTimeMillis() / 1000;
            mapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), file);
        } catch (IOException e) {
            System.err.println("Failed to write save slot " + slotId + ": " + e.getMessage());
        }
    }

    private Path resolveSlotPath(int slotId) {
        return saveDir.resolve("save-slot-" + slotId + ".json");
    }

    private static class SlotFile {
        public int slotId;
        public String name;
        public PlayerProfile profile;
        public long lastPlayed;
    }
}
