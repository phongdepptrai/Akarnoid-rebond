package com.arcade.arkanoid.menu.save;

import com.arcade.arkanoid.profile.PlayerProfile;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SampleSaveRepository {
    private static final Path SAVE_DIR = Paths.get("data", "profiles");
    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

    public List<SaveSlotSummary> loadSlots(int maxSlots) {
        List<SaveSlotSummary> summaries = new ArrayList<>();
        for (int slot = 1; slot <= maxSlots; slot++) {
            summaries.add(loadSlot(slot));
        }
        return summaries;
    }

    private SaveSlotSummary loadSlot(int slotId) {
        Path path = SAVE_DIR.resolve("save-slot-" + slotId + ".json");
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
        Path path = SAVE_DIR.resolve("save-slot-" + slotId + ".json");
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            System.err.println("Failed to delete save slot " + slotId + ": " + e.getMessage());
        }
    }

    public void writeSlot(int slotId, PlayerProfile profile) {
        profile.ensureDefaults();
        Path path = SAVE_DIR.resolve("save-slot-" + slotId + ".json");
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

    private static class SlotFile {
        public int slotId;
        public String name;
        public PlayerProfile profile;
        public long lastPlayed;
    }
}
