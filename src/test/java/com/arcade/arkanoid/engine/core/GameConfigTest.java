package com.arcade.arkanoid.engine.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class GameConfigTest {
    @Test
    void defaultConfigHasExpectedValues() {
        GameConfig config = GameConfig.defaultConfig();

        assertNotNull(config, "Default config should not be null");
        assertEquals(1280, config.width(), "Unexpected width");
        assertEquals(720, config.height(), "Unexpected height");
        assertEquals("Arkanoid Reborn", config.title(), "Unexpected title");
        assertEquals(60, config.targetFps(), "Unexpected target FPS");
    }

    @ParameterizedTest
    @MethodSource("configSamples")
    void widthReflectsConfiguredValue(
            GameConfig config, int expectedWidth, int expectedHeight, String expectedTitle, int expectedTargetFps) {
        assertEquals(expectedWidth, config.width(), "Width getter should expose constructor value");
    }

    @ParameterizedTest
    @MethodSource("configSamples")
    void heightReflectsConfiguredValue(
            GameConfig config, int expectedWidth, int expectedHeight, String expectedTitle, int expectedTargetFps) {
        assertEquals(expectedHeight, config.height(), "Height getter should expose constructor value");
    }

    @ParameterizedTest
    @MethodSource("configSamples")
    void titleReflectsConfiguredValue(
            GameConfig config, int expectedWidth, int expectedHeight, String expectedTitle, int expectedTargetFps) {
        assertEquals(expectedTitle, config.title(), "Title getter should expose constructor value");
    }

    @ParameterizedTest
    @MethodSource("configSamples")
    void targetFpsReflectsConfiguredValue(
            GameConfig config, int expectedWidth, int expectedHeight, String expectedTitle, int expectedTargetFps) {
        assertEquals(expectedTargetFps, config.targetFps(), "TargetFps getter should expose constructor value");
    }

    @Test
    void defaultConfigReturnsDistinctInstances() {
        GameConfig first = GameConfig.defaultConfig();
        GameConfig second = GameConfig.defaultConfig();

        assertNotSame(first, second, "Default config should return a new instance each time");
    }

    static Stream<Arguments> configSamples() {
        return Stream.of(
                Arguments.of(GameConfig.defaultConfig(), 1280, 720, "Arkanoid Reborn", 60),
                Arguments.of(new GameConfig(800, 600, "Test Config", 144), 800, 600, "Test Config", 144));
    }
}
