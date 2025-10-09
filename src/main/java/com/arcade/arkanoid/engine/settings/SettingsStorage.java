package com.arcade.arkanoid.engine.settings;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

class SettingsStorage {
    private final ObjectMapper mapper;
    private final Path filePath;

    SettingsStorage(Path filePath) {
        this.filePath = filePath;
        this.mapper = new ObjectMapper()
                .findAndRegisterModules()
                .enable(SerializationFeature.INDENT_OUTPUT);
    }

    GameSettings loadOrDefault() {
        if (Files.exists(filePath)) {
            try (InputStream stream = Files.newInputStream(filePath, StandardOpenOption.READ)) {
                GameSettings settings = mapper.readValue(stream, GameSettings.class);
                settings.ensureDefaults();
                return settings;
            } catch (IOException e) {
                System.err.println("Failed to read settings, using defaults: " + e.getMessage());
            }
        }
        return new GameSettings();
    }

    void save(GameSettings settings) {
        if (settings == null) {
            return;
        }
        settings.ensureDefaults();
        try {
            Files.createDirectories(filePath.getParent());
        } catch (IOException e) {
            System.err.println("Failed to create settings directory: " + e.getMessage());
        }
        try (OutputStream stream = Files.newOutputStream(filePath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
            mapper.writeValue(stream, settings);
        } catch (IOException e) {
            System.err.println("Failed to persist settings: " + e.getMessage());
        }
    }

    Path getFilePath() {
        return filePath;
    }
}
