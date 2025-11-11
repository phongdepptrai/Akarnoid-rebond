package com.arcade.arkanoid.menu.worldmap;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.arcade.arkanoid.gameplay.levels.LevelDefinition;
import com.arcade.arkanoid.gameplay.levels.LevelManager;
import com.arcade.arkanoid.profile.PlayerProfile;
import com.arcade.arkanoid.testutil.TestContextFactory;
import java.awt.Rectangle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class WorldMapSceneTest {
  private static Method attemptStartLevel;
  private static Field statusMessageField;
  private static Field levelManagerField;

  @BeforeAll
  static void cacheReflectionHandles() throws Exception {
    attemptStartLevel = WorldMapScene.class.getDeclaredMethod("attemptStartLevel", LevelNode.class);
    attemptStartLevel.setAccessible(true);
    statusMessageField = WorldMapScene.class.getDeclaredField("statusMessage");
    statusMessageField.setAccessible(true);
    levelManagerField = WorldMapScene.class.getDeclaredField("levelManager");
    levelManagerField.setAccessible(true);
  }

  @Test
  void lockedNodeDisplaysGateMessage() throws Exception {
    TestContextFactory.ContextBundle bundle = TestContextFactory.create();
    WorldMapScene scene = new WorldMapScene(bundle.context());

    LevelNode node = new LevelNode(sampleLevel(scene), 0, new Rectangle(0, 0, 60, 60));
    node.setUnlocked(false);
    node.setGateMessage("Locked behind gate");

    invokeAttempt(scene, node);

    assertEquals("Locked behind gate", readStatus(scene), "Status should mirror the gate message");
  }

  @Test
  void lackOfLivesPreventsLevelStart() throws Exception {
    TestContextFactory.ContextBundle bundle = TestContextFactory.create();
    PlayerProfile profile = bundle.profileManager().getActiveProfile();
    profile.setLives(0);
    profile.setMaxLives(5);

    WorldMapScene scene = new WorldMapScene(bundle.context());
    LevelNode node = unlockedNode(scene);

    invokeAttempt(scene, node);

    assertEquals(
        bundle.localizationService().translate("worldMap.noLives"),
        readStatus(scene),
        "Status should explain missing lives");
    assertEquals(0, profile.getLives(), "Economy should not modify lives when purchase fails");
  }

  @Test
  void missingGameplaySceneRefundsLifeAndShowsError() throws Exception {
    TestContextFactory.ContextBundle bundle = TestContextFactory.create();
    PlayerProfile profile = bundle.profileManager().getActiveProfile();
    profile.setLives(2);
    profile.setMaxLives(5);

    WorldMapScene scene = new WorldMapScene(bundle.context());
    LevelNode node = unlockedNode(scene);

    invokeAttempt(scene, node);

    assertEquals(
        2, profile.getLives(), "Life should be refunded when gameplay scene is unavailable");
    assertEquals(
        bundle.localizationService().translate("worldMap.loadingError"),
        readStatus(scene),
        "Status should describe the loading issue");
  }

  private static void invokeAttempt(WorldMapScene scene, LevelNode node) throws Exception {
    attemptStartLevel.invoke(scene, node);
  }

  private static String readStatus(WorldMapScene scene) throws Exception {
    return (String) statusMessageField.get(scene);
  }

  private static LevelNode unlockedNode(WorldMapScene scene) throws Exception {
    LevelNode node = new LevelNode(sampleLevel(scene), 0, new Rectangle(0, 0, 60, 60));
    node.setUnlocked(true);
    node.setGateMessage("");
    return node;
  }

  private static LevelDefinition sampleLevel(WorldMapScene scene) throws Exception {
    LevelManager manager = (LevelManager) levelManagerField.get(scene);
    manager.reset();
    return manager.current();
  }
}
