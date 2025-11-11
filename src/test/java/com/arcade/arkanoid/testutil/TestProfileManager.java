package com.arcade.arkanoid.testutil;

import com.arcade.arkanoid.profile.ProfileManager;
import java.nio.file.Path;

/** Profile manager variant that suppresses disk persistence for fast, deterministic tests. */
public class TestProfileManager extends ProfileManager {
  public TestProfileManager(Path path) {
    super(path);
  }

  @Override
  public void saveProfile() {
    onProfileSaved(getActiveProfile());
  }

  @Override
  public void saveProfileSync() {
    onProfileSaved(getActiveProfile());
  }
}
