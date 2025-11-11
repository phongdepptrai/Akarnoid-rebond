package com.arcade.arkanoid.menu.shop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcade.arkanoid.localization.LocalizationService;
import com.arcade.arkanoid.profile.PlayerProfile;
import com.arcade.arkanoid.testutil.TestContextFactory;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ShopSceneTest {
  private static Class<?> shopItemClass;
  private static Field shopItemIdField;
  private static Field statusMessageField;
  private static Method purchaseMethod;

  @BeforeAll
  static void loadReflectionMetadata() throws Exception {
    shopItemClass = Class.forName("com.arcade.arkanoid.menu.shop.ShopScene$ShopItem");
    shopItemIdField = shopItemClass.getDeclaredField("id");
    shopItemIdField.setAccessible(true);
    statusMessageField = ShopScene.class.getDeclaredField("statusMessage");
    statusMessageField.setAccessible(true);
    purchaseMethod = ShopScene.class.getDeclaredMethod("purchase", shopItemClass);
    purchaseMethod.setAccessible(true);
  }

  @Test
  void purchasingNewPaddleSkinConsumesCoinsAndEquips() throws Exception {
    TestContextFactory.ContextBundle bundle = TestContextFactory.create();
    PlayerProfile profile = bundle.profileManager().getActiveProfile();
    profile.getOwnedPaddleSkins().clear();
    profile.addPaddleSkin("classic");
    profile.setActivePaddleSkin("classic");
    profile.setCoins(1000);

    ShopScene scene = new ShopScene(bundle.context());
    Object neonStream = findShopItem(scene, "paddleItems", "neon-stream");

    invokePurchase(scene, neonStream);

    assertTrue(profile.hasPaddleSkin("neon-stream"), "Purchase should add the paddle skin");
    assertEquals("neon-stream", profile.getActivePaddleSkin(), "New skin should become active");
    assertEquals(700, profile.getCoins(), "Coins should decrease by the item price");
    assertEquals(
        translate(bundle.localizationService(), "shop.equipped", "shop.item.paddle.neon"),
        readStatus(scene),
        "Status should confirm equipment");
  }

  @Test
  void purchasingBuffAwardsConfiguredRewards() throws Exception {
    TestContextFactory.ContextBundle bundle = TestContextFactory.create();
    PlayerProfile profile = bundle.profileManager().getActiveProfile();
    profile.setCoins(300);
    profile.setLives(2);
    profile.setMaxLives(5);

    ShopScene scene = new ShopScene(bundle.context());
    Object extraLives = findShopItem(scene, "buffItems", "buff-extra-lives");

    invokePurchase(scene, extraLives);

    assertEquals(80, profile.getCoins(), "Coins should reflect cost deduction");
    assertEquals(4, profile.getLives(), "Buff should grant the configured number of lives");
    assertEquals(
        translate(bundle.localizationService(), "shop.buff.applied", "shop.item.buff.extraLives"),
        readStatus(scene),
        "Status should announce the applied buff");
  }

  @Test
  void purchaseFailsWhenInsufficientCoins() throws Exception {
    TestContextFactory.ContextBundle bundle = TestContextFactory.create();
    PlayerProfile profile = bundle.profileManager().getActiveProfile();
    profile.setCoins(100);
    profile.getOwnedBallSkins().remove("ion-burst");
    profile.setActiveBallSkin("classic");

    ShopScene scene = new ShopScene(bundle.context());
    Object ionBurst = findShopItem(scene, "ballItems", "ion-burst");

    invokePurchase(scene, ionBurst);

    assertFalse(profile.hasBallSkin("ion-burst"), "Skin should remain locked");
    assertEquals(100, profile.getCoins(), "Coins should stay unchanged");
    assertEquals(
        bundle.localizationService().translate("shop.notEnough"),
        readStatus(scene),
        "Status should explain the failure");
  }

  private static Object findShopItem(ShopScene scene, String listFieldName, String id)
      throws Exception {
    Field listField = ShopScene.class.getDeclaredField(listFieldName);
    listField.setAccessible(true);
    @SuppressWarnings("unchecked")
    List<Object> items = (List<Object>) listField.get(scene);
    for (Object item : items) {
      String value = (String) shopItemIdField.get(item);
      if (id.equals(value)) {
        return item;
      }
    }
    throw new IllegalArgumentException("No shop item with id " + id);
  }

  private static void invokePurchase(ShopScene scene, Object item) throws Exception {
    purchaseMethod.invoke(scene, item);
  }

  private static String readStatus(ShopScene scene) throws Exception {
    return (String) statusMessageField.get(scene);
  }

  private static String translate(
      LocalizationService localization, String messageKey, String itemKey) {
    return localization.translate(messageKey, localization.translate(itemKey));
  }
}
