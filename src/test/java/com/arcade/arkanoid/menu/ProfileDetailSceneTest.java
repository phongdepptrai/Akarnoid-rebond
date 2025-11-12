package com.arcade.arkanoid.menu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcade.arkanoid.profile.PlayerProfile;
import com.arcade.arkanoid.testutil.TestContextFactory;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ProfileDetailSceneTest {
  private static Method dailyBonusStatusMethod;

  @BeforeAll
  static void cacheReflectionMetadata() throws Exception {
    dailyBonusStatusMethod =
        ProfileDetailScene.class.getDeclaredMethod(
            "getDailyBonusStatus", PlayerProfile.class);
    dailyBonusStatusMethod.setAccessible(true);
  }

  @Test
  void dailyBonusStatusUsesAvailableMessageWhenNeverClaimed() throws Exception {
    TestContextFactory.ContextBundle bundle = TestContextFactory.create();
    bundle.localizationService().setLocale(Locale.ENGLISH);
    PlayerProfile profile = bundle.profileManager().getActiveProfile();
    profile.setLastDailyBonusEpochSeconds(0);

    ProfileDetailScene scene = new ProfileDetailScene(bundle.context());

    String status = invokeDailyBonus(scene, profile);

    assertEquals(
        bundle.localizationService().translate("profile.dailyBonus.available"), status);
  }

  @Test
  void dailyBonusStatusBecomesReadyAfterNextDayStarts() throws Exception {
    TestContextFactory.ContextBundle bundle = TestContextFactory.create();
    bundle.localizationService().setLocale(Locale.ENGLISH);
    PlayerProfile profile = bundle.profileManager().getActiveProfile();
    profile.setLastDailyBonusEpochSeconds(
        Instant.now().minus(Duration.ofDays(2)).getEpochSecond());

    ProfileDetailScene scene = new ProfileDetailScene(bundle.context());

    String status = invokeDailyBonus(scene, profile);

    assertEquals(
        bundle.localizationService().translate("profile.dailyBonus.ready"), status);
  }

  @Test
  void dailyBonusStatusShowsCountdownBeforeNextClaimWindow() throws Exception {
    TestContextFactory.ContextBundle bundle = TestContextFactory.create();
    bundle.localizationService().setLocale(Locale.ENGLISH);
    PlayerProfile profile = bundle.profileManager().getActiveProfile();
    profile.setLastDailyBonusEpochSeconds(Instant.now().getEpochSecond());

    ProfileDetailScene scene = new ProfileDetailScene(bundle.context());

    String status = invokeDailyBonus(scene, profile);

    assertTrue(
        status.matches("Next bonus in: \\d{2}:\\d{2}:\\d{2}"),
        "Countdown should follow the localized HH:MM:SS format");
  }

  private static String invokeDailyBonus(ProfileDetailScene scene, PlayerProfile profile)
      throws Exception {
    return (String) dailyBonusStatusMethod.invoke(scene, profile);
  }
}
