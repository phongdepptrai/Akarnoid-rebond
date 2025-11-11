package com.arcade.arkanoid.gameplay.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Color;
import org.junit.jupiter.api.Test;

class PaddleTest {
  private static Paddle newPaddle() {
    return new Paddle(100, 200, 120, 18, 300, Color.BLUE);
  }

  @Test
  void moveLeftRightAndStopAdjustVelocity() {
    Paddle paddle = newPaddle();

    paddle.moveLeft();
    assertEquals(-300, paddle.getVelocity().x, 0.0001, "moveLeft should apply negative speed");

    paddle.moveRight();
    assertEquals(300, paddle.getVelocity().x, 0.0001, "moveRight should apply positive speed");

    paddle.stop();
    assertEquals(0, paddle.getVelocity().x, 0.0001, "stop should zero horizontal velocity");
  }

  @Test
  void updateMovesPaddleUsingVelocity() {
    Paddle paddle = newPaddle();
    paddle.moveRight();

    paddle.update(0.5);

    assertEquals(
        250, paddle.getPosition().x, 0.0001, "Paddle should move speed * delta to the right");
  }

  @Test
  void resetWidthRestoresBaseWidth() {
    Paddle paddle = newPaddle();
    paddle.setWidth(200);
    assertEquals(200, paddle.getWidth(), 0.0001, "setWidth should immediately change width");

    paddle.resetWidth();

    assertEquals(
        120, paddle.getWidth(), 0.0001, "resetWidth should revert to original constructor width");
  }

  @Test
  void clampPreventsLeavingAllowedRange() {
    Paddle paddle = newPaddle();

    paddle.getPosition().x = -50;
    paddle.clamp(0, 400);
    assertEquals(
        0, paddle.getPosition().x, 0.0001, "Clamp should prevent moving left of min bound");

    paddle.getPosition().x = 350;
    paddle.clamp(0, 400);
    assertEquals(
        280,
        paddle.getPosition().x,
        0.0001,
        "Clamp should ensure right edge stays within max bound");
  }
}
