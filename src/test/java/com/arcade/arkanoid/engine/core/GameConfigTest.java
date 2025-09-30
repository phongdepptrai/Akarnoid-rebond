package com.arcade.arkanoid.engine.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class GameConfigTest {
    @Test
    void defaultConfigHasExpectedValues() {
        GameConfig config = GameConfig.defaultConfig();

        assertNotNull(config, "Default config should not be null");
        assertEquals(960, config.width(), "Unexpected width");
        assertEquals(720, config.height(), "Unexpected height");
        assertEquals("Arkanoid Reborn", config.title(), "Unexpected title");
        assertEquals(60, config.targetFps(), "Unexpected target FPS");
    }
}
