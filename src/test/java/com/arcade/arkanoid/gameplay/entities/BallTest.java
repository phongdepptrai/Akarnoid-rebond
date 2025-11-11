package com.arcade.arkanoid.gameplay.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import org.junit.jupiter.api.Test;

class BallTest {
  @Test
  void updateMovesBallAccordingToVelocity() {
    Ball ball = new Ball(50, 75, 12, Color.WHITE);
    ball.setVelocity(120, -60);

    ball.update(0.5);

    assertEquals(110, ball.getPosition().x, 0.0001, "X position should advance by vx * dt");
    assertEquals(45, ball.getPosition().y, 0.0001, "Y position should advance by vy * dt");
  }

  @Test
  void invertXAndYFlipVelocityDirection() {
    Ball ball = new Ball(0, 0, 10, Color.WHITE);
    ball.setVelocity(30, -45);

    ball.invertX();
    ball.invertY();

    assertEquals(-30, ball.getVelocity().x, 0.0001, "invertX should flip horizontal velocity");
    assertEquals(45, ball.getVelocity().y, 0.0001, "invertY should flip vertical velocity");
  }

  @Test
  void resetPositionMovesBallAndHaltsMomentum() {
    Ball ball = new Ball(20, 20, 10, Color.WHITE);
    ball.setVelocity(10, 15);

    ball.resetPosition(200, 175);

    assertEquals(200, ball.getPosition().x, 0.0001, "Position X should be reset");
    assertEquals(175, ball.getPosition().y, 0.0001, "Position Y should be reset");
    assertEquals(0, ball.getVelocity().x, 0.0001, "Velocity X should be cleared");
    assertEquals(0, ball.getVelocity().y, 0.0001, "Velocity Y should be cleared");
  }

  @Test
  void fireStateRemainsActiveUntilTimerExpires() {
    Ball ball = new Ball(0, 0, 10, Color.WHITE);
    ball.setVelocity(50, 0); // ensure update processes trail logic

    ball.setFire(2.0);
    assertTrue(ball.isFireActive(), "Ball should enter fire state immediately");

    ball.update(1.0);
    assertTrue(ball.isFireActive(), "Fire should remain active while timer above zero");

    ball.update(1.1);
    assertFalse(ball.isFireActive(), "Fire should deactivate once timer elapses");
  }

  @Test
  void duplicateCopiesStateIncludingVelocityAndFire() {
    Ball ball = new Ball(100, 100, 12, Color.WHITE);
    ball.setVelocity(80, -20);
    ball.setFire(1.5);

    Ball copy = ball.duplicate();

    assertNotSame(ball, copy, "Duplicate should create a fresh instance");
    assertEquals(
        ball.getPosition().x, copy.getPosition().x, 0.0001, "Duplicate should copy X position");
    assertEquals(
        ball.getPosition().y, copy.getPosition().y, 0.0001, "Duplicate should copy Y position");
    assertEquals(
        ball.getVelocity().x,
        copy.getVelocity().x,
        0.0001,
        "Duplicate should copy horizontal velocity");
    assertEquals(
        ball.getVelocity().y,
        copy.getVelocity().y,
        0.0001,
        "Duplicate should copy vertical velocity");
    assertTrue(copy.isFireActive(), "Duplicate should retain fire state");
    assertEquals(ball.getFillColor(), copy.getFillColor(), "Base fill color should be preserved");
    assertEquals(
        ball.getBorderColor(), copy.getBorderColor(), "Base border color should be preserved");
  }
}
