package com.arcade.arkanoid.testutil;

import com.arcade.arkanoid.economy.EconomyService;
import com.arcade.arkanoid.engine.assets.AssetManager;
import com.arcade.arkanoid.engine.audio.SoundManager;
import com.arcade.arkanoid.engine.core.GameConfig;
import com.arcade.arkanoid.engine.core.GameContext;
import com.arcade.arkanoid.engine.input.InputManager;
import com.arcade.arkanoid.engine.scene.SceneManager;
import com.arcade.arkanoid.engine.settings.SettingsManager;
import com.arcade.arkanoid.localization.LocalizationService;
import com.arcade.arkanoid.profile.ProfileManager;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/** Utility for building {@link GameContext} instances wired with lightweight test doubles. */
public final class TestContextFactory {
  private TestContextFactory() {}

  public static ContextBundle create() {
    GameConfig config = new GameConfig(800, 600, "Test Harness", 60);
    InputManager inputManager = new InputManager();
    SoundManager soundManager = new SoundManager();
    AssetManager assetManager = new AssetManager();
    SceneManager sceneManager = new SceneManager();
    TestProfileManager profileManager = new TestProfileManager(tempProfilePath());
    EconomyService economyService = new EconomyService(profileManager);
    TestSettingsManager settingsManager = new TestSettingsManager(tempSettingsPath());
    LocalizationService localizationService = new LocalizationService(settingsManager);
    GameContext context =
        new GameContext(
            null,
            null,
            inputManager,
            soundManager,
            assetManager,
            sceneManager,
            config,
            profileManager,
            economyService,
            settingsManager,
            localizationService);
    sceneManager.bindContext(context);
    return new ContextBundle(
        context,
        profileManager,
        economyService,
        inputManager,
        sceneManager,
        localizationService,
        settingsManager);
  }

  private static Path tempProfilePath() {
    return ensureDirectory(Paths.get("target", "test-data", "profiles"))
        .resolve(randomFileName("profile"));
  }

  private static Path tempSettingsPath() {
    return ensureDirectory(Paths.get("target", "test-data", "settings"))
        .resolve(randomFileName("settings"));
  }

  private static Path ensureDirectory(Path directory) {
    try {
      Files.createDirectories(directory);
    } catch (Exception ignored) {
    }
    return directory;
  }

  private static String randomFileName(String prefix) {
    return prefix + "-" + UUID.randomUUID() + ".json";
  }

  public static final class ContextBundle {
    private final GameContext context;
    private final ProfileManager profileManager;
    private final EconomyService economyService;
    private final InputManager inputManager;
    private final SceneManager sceneManager;
    private final LocalizationService localizationService;
    private final SettingsManager settingsManager;

    private ContextBundle(
        GameContext context,
        ProfileManager profileManager,
        EconomyService economyService,
        InputManager inputManager,
        SceneManager sceneManager,
        LocalizationService localizationService,
        SettingsManager settingsManager) {
      this.context = context;
      this.profileManager = profileManager;
      this.economyService = economyService;
      this.inputManager = inputManager;
      this.sceneManager = sceneManager;
      this.localizationService = localizationService;
      this.settingsManager = settingsManager;
    }

    public GameContext context() {
      return context;
    }

    public ProfileManager profileManager() {
      return profileManager;
    }

    public EconomyService economyService() {
      return economyService;
    }

    public InputManager inputManager() {
      return inputManager;
    }

    public SceneManager sceneManager() {
      return sceneManager;
    }

    public LocalizationService localizationService() {
      return localizationService;
    }

    public SettingsManager settingsManager() {
      return settingsManager;
    }
  }
}
