package com.arcade.arkanoid.profile;

import com.arcade.arkanoid.engine.util.IOThreadPool;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

/**
 * Manages the active profile and integrates save/load lifecycle.
 * Uses I/O Thread Pool for async save/load operations.
 */
public class ProfileManager {
    private final ProfileStorage storage;
    private PlayerProfile activeProfile;
    private final IOThreadPool ioThreadPool;

    public ProfileManager() {
        this(defaultProfilePath());
    }

    public ProfileManager(Path profilePath) {
        this.storage = new ProfileStorage(profilePath);
        this.ioThreadPool = IOThreadPool.getInstance();
        this.activeProfile = storage.loadOrCreateDefault();
        ensureLoginMetadata();
    }

    private static Path defaultProfilePath() {
        // Use project-local data directory instead of user home
        return Paths.get("data", "profiles", "default-profile.json");
    }

    public PlayerProfile getActiveProfile() {
        return activeProfile;
    }

    /**
     * Save profile asynchronously on I/O Thread.
     */
    public void saveProfile() {
        ioThreadPool.submit(() -> {
            storage.save(activeProfile);
        });
    }

    /**
     * Save profile synchronously (blocking).
     */
    public void saveProfileSync() {
        storage.save(activeProfile);
    }

    public void refreshProfile(PlayerProfile updatedProfile) {
        if (updatedProfile == null) {
            return;
        }
        updatedProfile.ensureDefaults();
        this.activeProfile = updatedProfile;
        saveProfile();
    }

    /**
     * Updates login metadata, ensuring daily streak tracking remains consistent.
     */
    private void ensureLoginMetadata() {
        PlayerProfile profile = activeProfile;
        long now = Instant.now().getEpochSecond();
        LocalDate today = Instant.ofEpochSecond(now).atZone(ZoneOffset.UTC).toLocalDate();
        LocalDate lastLogin = Instant.ofEpochSecond(profile.getLastLoginEpochSeconds()).atZone(ZoneOffset.UTC)
                .toLocalDate();

        if (!today.equals(lastLogin)) {
            profile.setLastLoginEpochSeconds(now);
        }
    }
}
