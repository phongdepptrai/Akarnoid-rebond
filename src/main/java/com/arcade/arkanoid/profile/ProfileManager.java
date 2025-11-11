package com.arcade.arkanoid.profile;

import com.arcade.arkanoid.engine.util.IOThreadPool;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.function.Consumer;

/**
 * Manages the active profile and integrates save/load lifecycle.
 * Uses I/O Thread Pool for async save/load operations.
 */
public class ProfileManager {
    private final ProfileStorage storage;
    private PlayerProfile activeProfile;
    private final IOThreadPool ioThreadPool;
    private volatile Consumer<PlayerProfile> postSaveListener;

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
        PlayerProfile profile = activeProfile;
        ioThreadPool.submit(() -> {
            storage.save(profile);
            onProfileSaved(profile);
        });
    }

    /**
     * Save profile synchronously (blocking).
     */
    public void saveProfileSync() {
        PlayerProfile profile = activeProfile;
        storage.save(profile);
        onProfileSaved(profile);
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
     * Registers a callback that runs every time the active profile is persisted.
     */
    public void setPostSaveListener(Consumer<PlayerProfile> listener) {
        this.postSaveListener = listener;
    }

    /**
     * Invoked after the profile finishes persisting to disk. Subclasses can override to keep
     * test doubles lightweight while still reusing the listener dispatching logic.
     */
    protected void onProfileSaved(PlayerProfile profile) {
        Consumer<PlayerProfile> listener = postSaveListener;
        if (listener == null || profile == null) {
            return;
        }
        try {
            listener.accept(profile);
        } catch (Exception e) {
            System.err.println("Post-save listener failed: " + e.getMessage());
        }
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
