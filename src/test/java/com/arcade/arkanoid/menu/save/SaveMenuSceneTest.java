package com.arcade.arkanoid.menu.save;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcade.arkanoid.ArkanoidGame;
import com.arcade.arkanoid.engine.core.GameContext;
import com.arcade.arkanoid.engine.scene.Scene;
import com.arcade.arkanoid.profile.PlayerProfile;
import com.arcade.arkanoid.testutil.TestContextFactory;
import java.awt.Graphics2D;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class SaveMenuSceneTest {

  @Test
  void selectingSlotKeepsPurchasedSkinsWhenReloaded() throws Exception {
    Path saveDir = Files.createTempDirectory("save-menu-test");
    SampleSaveRepository repository = new SampleSaveRepository(saveDir);
    TestContextFactory.ContextBundle bundle = TestContextFactory.create();
    bundle.sceneManager().register(ArkanoidGame.SCENE_MAP, DummyScene::new);

    SaveMenuScene scene = new SaveMenuScene(bundle.context(), repository);

    PlayerProfile profile = PlayerProfile.newDefault();
    profile.setDisplayName("Test Pilot");
    repository.writeSlot(1, profile);

    scene.onEnter();
    invokeActivateSelectedSlot(scene);

    PlayerProfile active = bundle.profileManager().getActiveProfile();
    active.addPaddleSkin("neon-stream");
    bundle.profileManager().saveProfileSync();

    SaveSlotSummary summary = repository.loadSlots(1).get(0);
    assertTrue(
        summary.getProfile().hasPaddleSkin("neon-stream"),
        "Active slot should retain purchased paddle skins");
  }

  private static void invokeActivateSelectedSlot(SaveMenuScene scene) throws Exception {
    Method method = SaveMenuScene.class.getDeclaredMethod("activateSelectedSlot");
    method.setAccessible(true);
    method.invoke(scene);
  }

  private static final class DummyScene extends Scene {
    DummyScene(GameContext context) {
      super(context);
    }

    @Override
    public void update(double deltaTime) {}

    @Override
    public void render(Graphics2D graphics) {}
  }
}
