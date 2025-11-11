package com.arcade.arkanoid.testutil;

import com.arcade.arkanoid.engine.settings.SettingsManager;
import java.nio.file.Path;
import java.util.Locale;

/** Settings manager override that keeps locale changes in-memory. */
public class TestSettingsManager extends SettingsManager {
  private Locale locale = Locale.ENGLISH;

  public TestSettingsManager(Path path) {
    super(path);
  }

  @Override
  public Locale resolveLocale() {
    return locale;
  }

  @Override
  public void setLocale(Locale locale) {
    if (locale == null) {
      return;
    }
    this.locale = locale;
  }

  @Override
  public void save() {
    // Skip file writes to keep tests self-contained.
  }
}
