package com.arcade.arkanoid.gameplay;

import com.arcade.arkanoid.ArkanoidGame;
import com.arcade.arkanoid.engine.core.GameContext;
import com.arcade.arkanoid.engine.input.InputManager;
import com.arcade.arkanoid.engine.scene.Scene;
import com.arcade.arkanoid.engine.scene.SceneManager;
import com.arcade.arkanoid.gameplay.entities.Ball;
import com.arcade.arkanoid.gameplay.entities.Brick;
import com.arcade.arkanoid.gameplay.entities.Paddle;
import com.arcade.arkanoid.gameplay.entities.PowerUp;
import com.arcade.arkanoid.gameplay.levels.LevelDefinition;
import com.arcade.arkanoid.gameplay.levels.LevelManager;
import com.arcade.arkanoid.gameplay.objectives.ObjectiveEngine;
import com.arcade.arkanoid.gameplay.objectives.StandardObjectiveEngine;
import com.arcade.arkanoid.gameplay.system.HudRenderer;
import com.arcade.arkanoid.localization.LocalizationService;
import com.arcade.arkanoid.menu.PauseScene;
import com.arcade.arkanoid.profile.PlayerProfile;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class GameplayScene extends Scene {
    private static final double BASE_PADDLE_WIDTH = 120;
    private static final double PADDLE_HEIGHT = 18;
    private static final double PADDLE_SPEED = 420;
    private static final double BALL_SIZE = 14;
    private static final double BASE_BALL_SPEED = 360;
    private static final double PADDLE_Y_OFFSET = 60;
    private static final double POWERUP_SIZE = 18;
    private static final double POWERUP_DROP_CHANCE = 0.15;

    private final LevelManager levelManager = new LevelManager();
    private final HudRenderer hudRenderer;
    private final Random random = new Random();
    private final List<Brick> bricks = new ArrayList<>();
    private final List<PowerUp> powerUps = new ArrayList<>();
    private final ObjectiveEngine objectiveEngine = new StandardObjectiveEngine();
    private final ObjectiveEngine.Listener objectiveListener = new SceneObjectiveListener();

    private Paddle paddle;
    private Ball ball;
    private double currentBallSpeed;
    private int score = 0;
    private int lives = 3;
    private boolean awaitingLaunch = true;
    private boolean paused = false;
    private boolean initialized = false;
    private boolean stageCleared = false;
    private boolean gameOver = false;
    private String statusMessage = "";
    private LevelDefinition activeLevel;
    private final LocalizationService localization;

    public GameplayScene(GameContext context) {
        super(context);
        this.localization = context.getLocalizationService();
        this.hudRenderer = new HudRenderer(localization);
    }

    @Override
    public void onEnter() {
        if (!initialized) {
            startNewGame();
            initialized = true;
        }
        paused = false;
        statusMessage = "";
    }

    public void resumeFromPause() {
        paused = false;
    }

    public void pauseGame() {
        paused = true;
        SceneManager sceneManager = context.getScenes();
        PauseScene pauseScene = (PauseScene) sceneManager.getPersistentScene(ArkanoidGame.SCENE_PAUSE);
        if (pauseScene != null) {
            pauseScene.bindGameplay(this);
            sceneManager.switchTo(ArkanoidGame.SCENE_PAUSE);
        }
    }

    public void beginNewSession() {
        startNewGame();
    }

    public void restartLevel() {
        loadLevel();
    }

    public boolean isSessionActive() {
        return initialized;
    }

    private void startNewGame() {
        score = 0;
        lives = 3;
        PlayerProfile profile = context.getProfileManager().getActiveProfile();
        levelManager.resetToLevel(profile.getCurrentLevelId());
        loadLevel();
    }

    private void loadLevel() {
        bricks.clear();
        powerUps.clear();
        awaitingLaunch = true;
        stageCleared = false;
        gameOver = false;
        statusMessage = localization.translate("gameplay.message.ready");
        activeLevel = levelManager.current();
        objectiveEngine.bind(activeLevel, objectiveListener);
        objectiveEngine.resetProgress();
        paddle = new Paddle(
                (context.getConfig().width() - BASE_PADDLE_WIDTH) / 2.0,
                context.getConfig().height() - PADDLE_Y_OFFSET,
                BASE_PADDLE_WIDTH,
                PADDLE_HEIGHT,
                PADDLE_SPEED,
                new Color(0x00BCD4)
        );
        ball = new Ball(0, 0, BALL_SIZE, Color.WHITE);
        currentBallSpeed = BASE_BALL_SPEED;
        resetBall();
        buildBricks(activeLevel);
    }

    private void resetBall() {
        awaitingLaunch = true;
        ball.resetPosition(
                paddle.getPosition().x + paddle.getWidth() / 2.0 - BALL_SIZE / 2.0,
                paddle.getPosition().y - BALL_SIZE - 4
        );
        currentBallSpeed = Math.max(260, currentBallSpeed);
        ball.setVelocity(0, 0);
    }

    private void buildBricks(LevelDefinition definition) {
        int cols = definition.columns();
        int rows = definition.rows();
        if (cols <= 0 || rows <= 0) {
            return;
        }
        double horizontalPadding = 50;
        double verticalPadding = 90;
        double gap = 4;
        double availableWidth = context.getConfig().width() - horizontalPadding * 2;
        double brickWidth = (availableWidth - (cols - 1) * gap) / cols;
        double brickHeight = 24;

        definition.bricks().forEach(blueprint -> {
            int column = blueprint.column();
            int row = blueprint.row();
            if (column < 0 || column >= cols || row < 0 || row >= rows) {
                return;
            }
            double x = horizontalPadding + column * (brickWidth + gap);
            double y = verticalPadding + row * (brickHeight + gap);
            int hitPoints = Math.max(1, blueprint.hitPoints());
            int scoreValue = 50 * hitPoints;
            Brick brick = new Brick(
                    x,
                    y,
                    brickWidth,
                    brickHeight,
                    hitPoints,
                    scoreValue,
                    column,
                    row,
                    blueprint.brickType(),
                    blueprint.tags(),
                    blueprint.modifiers()
            );
            bricks.add(brick);
        });
    }

    @Override
    public void update(double deltaTime) {
        if (paused) {
            return;
        }

        InputManager input = context.getInput();

        if (input.isKeyJustPressed(KeyEvent.VK_ESCAPE)) {
            pauseGame();
            return;
        }

        if (gameOver) {
            handleGameOverInput(input);
            return;
        }

        handleMovementInput(input);

        paddle.update(deltaTime);
        paddle.clamp(0, context.getConfig().width());

        if (awaitingLaunch) {
            attachBallToPaddle();
            if (input.isKeyJustPressed(KeyEvent.VK_SPACE)) {
                launchBall();
            }
        } else {
            ball.update(deltaTime);
            constrainBallWithinArena();
            handlePaddleCollision();
            handleBrickCollisions();
        }

        updatePowerUps(deltaTime);

        objectiveEngine.update(deltaTime);

        if (!gameOver && !stageCleared && isLevelComplete()) {
            handleLevelCompletion();
        }
    }

    private boolean isLevelComplete() {
        if (objectiveEngine.arePrimaryObjectivesMet()) {
            return true;
        }
        return bricks.stream().allMatch(Brick::isDestroyed);
    }

    private void handleMovementInput(InputManager input) {
        boolean left = input.isKeyPressed(KeyEvent.VK_LEFT) || input.isKeyPressed(KeyEvent.VK_A);
        boolean right = input.isKeyPressed(KeyEvent.VK_RIGHT) || input.isKeyPressed(KeyEvent.VK_D);

        if (left && !right) {
            paddle.moveLeft();
        } else if (right && !left) {
            paddle.moveRight();
        } else {
            paddle.stop();
        }
    }

    private void attachBallToPaddle() {
        ball.getPosition().x = paddle.getPosition().x + paddle.getWidth() / 2.0 - BALL_SIZE / 2.0;
        ball.getPosition().y = paddle.getPosition().y - BALL_SIZE - 4;
    }

    private void launchBall() {
        awaitingLaunch = false;
        currentBallSpeed = BASE_BALL_SPEED;
        statusMessage = "";
        setBallVelocityByAngle(Math.toRadians(random.nextDouble() * 120 - 60));
    }

    private void setBallVelocityByAngle(double angle) {
        double vx = currentBallSpeed * Math.sin(angle);
        double vy = -Math.abs(currentBallSpeed * Math.cos(angle));
        ball.setVelocity(vx, vy);
    }

    private void constrainBallWithinArena() {
        double width = context.getConfig().width();
        double height = context.getConfig().height();
        boolean hitBoundary = false;

        if (ball.getPosition().x <= 0) {
            ball.getPosition().x = 0;
            ball.invertX();
            hitBoundary = true;
        } else if (ball.getPosition().x + ball.getWidth() >= width) {
            ball.getPosition().x = width - ball.getWidth();
            ball.invertX();
            hitBoundary = true;
        }

        if (ball.getPosition().y <= 50) {
            ball.getPosition().y = 50;
            ball.invertY();
            hitBoundary = true;
        }

        if (ball.getPosition().y > height) {
            loseLife();
            return;
        }

        if (hitBoundary) {
            normalizeBallSpeed();
        }
    }

    private void normalizeBallSpeed() {
        double vx = ball.getVelocity().x;
        double vy = ball.getVelocity().y;
        double length = Math.sqrt(vx * vx + vy * vy);
        if (length == 0) {
            return;
        }
        double scale = currentBallSpeed / length;
        ball.setVelocity(vx * scale, vy * scale);
    }

    private void handlePaddleCollision() {
        Rectangle2D ballBounds = ball.getBounds();
        Rectangle2D paddleBounds = paddle.getBounds();
        if (ballBounds.intersects(paddleBounds) && ball.getVelocity().y > 0) {
            double paddleCenter = paddleBounds.getCenterX();
            double ballCenter = ballBounds.getCenterX();
            double offset = (ballCenter - paddleCenter) / (paddle.getWidth() / 2.0);
            offset = Math.max(-1, Math.min(1, offset));
            double angle = Math.toRadians(60 * offset);
            currentBallSpeed = Math.min(560, currentBallSpeed * 1.02);
            setBallVelocityByAngle(angle);
            ball.getPosition().y = paddle.getPosition().y - BALL_SIZE - 1;
        }
    }

    private void handleBrickCollisions() {
        Rectangle2D ballBounds = ball.getBounds();
        for (Brick brick : bricks) {
            if (brick.isDestroyed()) {
                continue;
            }
            Rectangle2D brickBounds = brick.getBounds();
            if (brickBounds.intersects(ballBounds)) {
                Rectangle2D intersection = brickBounds.createIntersection(ballBounds);
                if (intersection.getWidth() >= intersection.getHeight()) {
                    ball.invertY();
                } else {
                    ball.invertX();
                }
                brick.hit();
                if (brick.isDestroyed()) {
                    score += brick.getScoreValue();
                    objectiveEngine.handleEvent(new ObjectiveEngine.ScoreAwardedEvent(brick.getScoreValue()));
                    objectiveEngine.handleEvent(new ObjectiveEngine.BrickClearedEvent(
                            brick.getGridColumn(),
                            brick.getGridRow(),
                            brick.getBlueprintType(),
                            brick.getTags()
                    ));
                    maybeSpawnPowerUp(brick);
                }
                normalizeBallSpeed();
                break;
            }
        }
    }

    private void maybeSpawnPowerUp(Brick brick) {
        if (random.nextDouble() > POWERUP_DROP_CHANCE) {
            return;
        }
        PowerUp.Type type = random.nextBoolean() ? PowerUp.Type.EXPAND_PADDLE : PowerUp.Type.SLOW_BALL;
        PowerUp powerUp = new PowerUp(
                brick.getPosition().x + brick.getWidth() / 2.0 - POWERUP_SIZE / 2.0,
                brick.getPosition().y + brick.getHeight() / 2.0 - POWERUP_SIZE / 2.0,
                POWERUP_SIZE,
                type,
                type == PowerUp.Type.EXPAND_PADDLE ? new Color(0x8BC34A) : new Color(0xFFEB3B)
        );
        powerUps.add(powerUp);
    }

    private void updatePowerUps(double deltaTime) {
        Iterator<PowerUp> iterator = powerUps.iterator();
        while (iterator.hasNext()) {
            PowerUp powerUp = iterator.next();
            powerUp.update(deltaTime);
            if (powerUp.getPosition().y > context.getConfig().height()) {
                iterator.remove();
                continue;
            }
            if (powerUp.getBounds().intersects(paddle.getBounds())) {
                applyPowerUp(powerUp.getType());
                iterator.remove();
            }
        }
    }

    private void applyPowerUp(PowerUp.Type type) {
        switch (type) {
            case EXPAND_PADDLE:
                paddle.setWidth(Math.min(paddle.getWidth() * 1.3, 240));
                break;
            case SLOW_BALL:
                currentBallSpeed = Math.max(260, currentBallSpeed * 0.8);
                normalizeBallSpeed();
                break;
            default:
                break;
        }
    }

    private void handleLevelCompletion() {
        if (stageCleared) {
            return;
        }
        stageCleared = true;
        awaitingLaunch = true;
        PlayerProfile profile = context.getProfileManager().getActiveProfile();
        profile.markLevelCompleted(activeLevel.id());
        context.getProfileManager().saveProfile();
        profile.unlockLevel(activeLevel.id());
        if (levelManager.hasNext()) {
            levelManager.advance();
            LevelDefinition nextLevel = levelManager.current();
            profile.unlockLevel(nextLevel.id());
            profile.setCurrentLevelId(nextLevel.id());
            context.getProfileManager().saveProfile();
            loadLevel();
            statusMessage = localization.translate("gameplay.message.stageCleared");
        } else {
            profile.setCurrentLevelId(activeLevel.id());
            context.getProfileManager().saveProfile();
            gameOver = true;
            statusMessage = localization.translate("gameplay.message.victory");
        }
    }

    private void loseLife() {
        lives--;
        if (lives <= 0) {
            gameOver = true;
            statusMessage = localization.translate("gameplay.message.gameOver");
        } else {
            statusMessage = localization.translate("gameplay.message.lifeLost");
            paddle.setWidth(BASE_PADDLE_WIDTH);
            currentBallSpeed = BASE_BALL_SPEED;
            resetBall();
        }
    }

    private void handleGameOverInput(InputManager input) {
        if (input.isKeyJustPressed(KeyEvent.VK_ENTER)) {
            startNewGame();
        } else if (input.isKeyJustPressed(KeyEvent.VK_ESCAPE)) {
            context.getScenes().switchTo(ArkanoidGame.SCENE_MENU);
        }
    }

    @Override
    public void render(Graphics2D graphics) {
        drawBackground(graphics);
        String levelName = activeLevel == null ? "Loading..." : activeLevel.displayName();
        hudRenderer.renderHud(graphics, score, lives, levelName, objectiveEngine.snapshot());

        bricks.forEach(brick -> brick.render(graphics));
        paddle.render(graphics);
        ball.render(graphics);
        powerUps.forEach(powerUp -> powerUp.render(graphics));

        if (paused) {
            graphics.setColor(new Color(0, 0, 0, 150));
            graphics.fillRect(0, 0, context.getConfig().width(), context.getConfig().height());
            hudRenderer.renderCenterMessage(graphics,
                    localization.translate("gameplay.message.paused"),
                    context.getConfig().width(),
                    context.getConfig().height());
        } else if (awaitingLaunch || gameOver || !statusMessage.isEmpty()) {
            hudRenderer.renderCenterMessage(graphics,
                    statusMessage.isEmpty() ? localization.translate("gameplay.prompt.launch") : statusMessage,
                    context.getConfig().width(),
                    context.getConfig().height());
        }
    }

    private void drawBackground(Graphics2D graphics) {
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setColor(new Color(18, 18, 30));
        graphics.fillRect(0, 0, context.getConfig().width(), context.getConfig().height());
        graphics.setColor(new Color(49, 49, 77));
        graphics.setStroke(new BasicStroke(2));
        graphics.drawRect(40, 50, context.getConfig().width() - 80, context.getConfig().height() - 110);
    }

    private static class SceneObjectiveListener implements ObjectiveEngine.Listener {
        @Override
        public void onObjectiveProgress(ObjectiveEngine.ObjectiveState state) {
            // Intentionally left blank; HUD pulls latest snapshot each frame.
        }

        @Override
        public void onObjectiveCompleted(ObjectiveEngine.ObjectiveState state) {
            // Future: hook celebrations or particle effects.
        }

        @Override
        public void onObjectiveFailed(ObjectiveEngine.ObjectiveState state) {
            // Future: trigger failure messaging or penalties.
        }
    }
}
