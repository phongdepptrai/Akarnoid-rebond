package com.arcade.arkanoid.profile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Responsible for reading and writing player profiles to disk.
 */
public class ProfileStorage {
    private final ObjectMapper mapper;
    private final Path profilePath;

    public ProfileStorage(Path profilePath) {
        this.profilePath = profilePath;
        this.mapper = new ObjectMapper()
                .findAndRegisterModules()
                .enable(SerializationFeature.INDENT_OUTPUT);
    }

    public PlayerProfile loadOrCreateDefault() {
        if (Files.exists(profilePath)) {
            try (InputStream stream = Files.newInputStream(profilePath, StandardOpenOption.READ)) {
                PlayerProfile profile = mapper.readValue(stream, PlayerProfile.class);
                profile.ensureDefaults();
                return profile;
            } catch (IOException e) {
                System.err.println("Failed to read player profile, creating new one: " + e.getMessage());
            }
        }
        PlayerProfile fallback = PlayerProfile.newDefault();
        save(fallback);
        return fallback;
    }

    public void save(PlayerProfile profile) {
        if (profile == null) {
            return;
        }
        profile.ensureDefaults();
        try {
            Files.createDirectories(profilePath.getParent());
        } catch (IOException e) {
            System.err.println("Failed to create profile directory: " + e.getMessage());
        }
        try (OutputStream stream = Files.newOutputStream(profilePath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
            mapper.writeValue(stream, profile);
        } catch (IOException e) {
            System.err.println("Failed to persist player profile: " + e.getMessage());
        }
    }

    public Path getProfilePath() {
        return profilePath;
    }
}
