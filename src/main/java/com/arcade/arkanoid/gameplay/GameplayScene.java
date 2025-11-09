package com.arcade.arkanoid.gameplay;

import com.arcade.arkanoid.ArkanoidGame;
import com.arcade.arkanoid.engine.assets.AssetManager;
import com.arcade.arkanoid.engine.core.GameContext;
import com.arcade.arkanoid.engine.input.InputManager;
import com.arcade.arkanoid.engine.scene.Scene;
import com.arcade.arkanoid.gameplay.entities.Ball;
import com.arcade.arkanoid.gameplay.entities.Brick;
import com.arcade.arkanoid.gameplay.entities.Paddle;
import com.arcade.arkanoid.gameplay.entities.PowerUp;
import com.arcade.arkanoid.gameplay.cosmetics.SkinCatalog;
import com.arcade.arkanoid.gameplay.levels.LevelDefinition;
import com.arcade.arkanoid.gameplay.levels.LevelManager;
import com.arcade.arkanoid.gameplay.objectives.ObjectiveEngine;
import com.arcade.arkanoid.gameplay.objectives.StandardObjectiveEngine;
import com.arcade.arkanoid.gameplay.system.GameplayPanelRenderer;
import com.arcade.arkanoid.gameplay.system.GameplayVisualEffects;
import com.arcade.arkanoid.gameplay.system.HudRenderer;
import com.arcade.arkanoid.gameplay.system.PowerUpController;
import com.arcade.arkanoid.gameplay.system.PaddleGunSystem;
import com.arcade.arkanoid.localization.LocalizationService;
import com.arcade.arkanoid.menu.PauseScene;
import com.arcade.arkanoid.profile.PlayerProfile;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
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
    private static final int MAX_LIVES = 9;
    private static final int MAX_SIMULTANEOUS_BALLS = 5;
    private static final double FIRE_BALL_DURATION_SECONDS = 8.0;

    // Frame/Panel constants
    public static final int SIDE_PANEL_WIDTH = GameplayPanelRenderer.getPanelWidth();

    private final LevelManager levelManager = new LevelManager();
    private final HudRenderer hudRenderer;
    private final GameplayPanelRenderer panelRenderer = GameplayPanelRenderer.getInstance();
    private final GameplayVisualEffects visualEffects = GameplayVisualEffects.getInstance();
    private final Random random = new Random();

    private final List<Brick> bricks = new ArrayList<>();
    private final List<Ball> balls = new ArrayList<>();
    private final List<Ball> pendingBalls = new ArrayList<>();
    private final ObjectiveEngine objectiveEngine = new StandardObjectiveEngine();
    private final ObjectiveEngine.Listener objectiveListener = new SceneObjectiveListener();
    private final PowerUpController powerUpController;
    private final PaddleGunSystem paddleGunSystem = new PaddleGunSystem();

    private Paddle paddle;
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
    private BufferedImage paddleImage = null;
    private BufferedImage backgroundImage = null;

    public GameplayScene(GameContext context) {
        super(context);
        this.localization = context.getLocalizationService();
        this.hudRenderer = new HudRenderer(localization);
        this.powerUpController = new PowerUpController(random, POWERUP_DROP_CHANCE, POWERUP_SIZE);
    }

    @Override
    public void onEnter() {
        loadPaddleImage();
        System.out.println("onen");
        loadBackgroundImage();
        if (!initialized) {
            startNewGame();
            initialized = true;
        }
        paused = false;
        statusMessage = "";
    }

    /**
     * Factory method to load paddle image asset.
     */
    private void loadPaddleImage() {
        if (paddleImage == null) {
            AssetManager assets = context.getAssets();
            assets.loadImage("paddle", "/graphics/paddle.PNG");
            System.out.println("load ok");
            paddleImage = assets.getImage("paddle");
        }
    }

    /**
     * Factory method to load background image asset.
     */
    private void loadBackgroundImage() {
        if (backgroundImage == null) {
            AssetManager assets = context.getAssets();
            assets.loadImage("gameplay-background", "/graphics/background2.jpg");
            backgroundImage = assets.getImage("gameplay-background");
        }
    }

    public void resumeFromPause() {
        paused = false;
    }

    public void pauseGame() {
        paused = true;
        PauseScene pauseScene = (PauseScene) context.getScenes().getPersistentScene(ArkanoidGame.SCENE_PAUSE);
        if (pauseScene != null) {
            pauseScene.bindGameplay(this);
            context.getScenes().switchTo(ArkanoidGame.SCENE_PAUSE);
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
        clearGameState();
        activeLevel = levelManager.current();
        objectiveEngine.bind(activeLevel, objectiveListener);
        objectiveEngine.resetProgress();

        createGameEntities();
        buildBricks(activeLevel);
    }

    /**
     * Factory method to clear game state.
     */
    private void clearGameState() {
        bricks.clear();
        powerUpController.reset();
        paddleGunSystem.reset();
        balls.clear();
        pendingBalls.clear();
        awaitingLaunch = true;
        stageCleared = false;
        gameOver = false;
        statusMessage = localization.translate("gameplay.message.ready");
    }

    /**
     * Factory method to create paddle and ball entities.
     */
    private void createGameEntities() {
        PlayerProfile profile = context.getProfileManager().getActiveProfile();
        SkinCatalog.PaddleSkin paddleSkin = SkinCatalog.paddleSkin(profile.getActivePaddleSkin());
        SkinCatalog.BallSkin ballSkin = SkinCatalog.ballSkin(profile.getActiveBallSkin());

        paddle = new Paddle(
                (context.getConfig().width() - BASE_PADDLE_WIDTH) / 2.0,
                context.getConfig().height() - PADDLE_Y_OFFSET,
                BASE_PADDLE_WIDTH,
                PADDLE_HEIGHT,
                PADDLE_SPEED,
                paddleSkin.fillColor(),
                paddleSkin.borderColor());
        if (paddleImage == null) {
            loadPaddleImage();
        }
        paddle.setPaddleImage(paddleImage);
        balls.add(createBall(ballSkin));
        currentBallSpeed = BASE_BALL_SPEED;
        resetBall();
    }

    private Ball primaryBall() {
        return balls.isEmpty() ? null : balls.get(0);
    }

    private Ball createBall(SkinCatalog.BallSkin skin) {
        return new Ball(0, 0, BALL_SIZE, skin.fillColor(), skin.borderColor());
    }

    private void addExtraBall(Ball ball) {
        if (ball != null) {
            pendingBalls.add(ball);
        }
    }

    private void resetBall() {
        PlayerProfile profile = context.getProfileManager().getActiveProfile();
        SkinCatalog.BallSkin ballSkin = SkinCatalog.ballSkin(profile.getActiveBallSkin());
        if (balls.isEmpty()) {
            balls.add(createBall(ballSkin));
        } else {
            Ball primary = primaryBall();
            if (primary != null) {
                primary.setBaseColors(ballSkin.fillColor(), ballSkin.borderColor());
            }
            if (balls.size() > 1) {
                Ball head = primary;
                balls.clear();
                if (head != null) {
                    balls.add(head);
                }
            }
        }
        Ball primary = primaryBall();
        awaitingLaunch = true;
        currentBallSpeed = BASE_BALL_SPEED;
        if (primary != null) {
            primary.resetPosition(
                    paddle.getPosition().x + paddle.getWidth() / 2.0 - BALL_SIZE / 2.0,
                    paddle.getPosition().y - BALL_SIZE - 4);
            primary.setVelocity(0, 0);
            primary.clearFire();
        }
        pendingBalls.clear();
    }

    private void buildBricks(LevelDefinition definition) {
        int cols = definition.columns();
        int rows = definition.rows();
        if (cols <= 0 || rows <= 0)
            return;

        double leftBoundary = SIDE_PANEL_WIDTH + 15;
        double rightBoundary = context.getConfig().width() - SIDE_PANEL_WIDTH - 15;
        double horizontalPadding = leftBoundary + 15; // Start bricks 15px inside left border
        double verticalPadding = 115; // Moved down 25px total (was 90, now 115)
        double gap = 4;
        double availableWidth = (rightBoundary - 15) - horizontalPadding;
        double brickWidth = (availableWidth - (cols - 1) * gap) / cols;
        double brickHeight = 24;

        java.util.Set<String> addedPositions = new java.util.HashSet<>();

        definition.bricks().forEach(blueprint -> {
            if (!isValidBrickPosition(blueprint, cols, rows, addedPositions))
                return;

            Brick brick = createBrick(blueprint, brickWidth, brickHeight, gap, horizontalPadding, verticalPadding);
            bricks.add(brick);
        });
    }

    /**
     * Validates brick position within grid boundaries and prevents duplicates.
     * 
     * @param blueprint      The brick blueprint containing position data
     * @param cols           Maximum number of columns in the grid
     * @param rows           Maximum number of rows in the grid
     * @param addedPositions Set tracking already-placed brick positions
     * @return true if position is valid and not duplicate, false otherwise
     */
    private boolean isValidBrickPosition(LevelDefinition.BrickBlueprint blueprint,
            int cols, int rows, java.util.Set<String> addedPositions) {
        if (blueprint == null) {
            return false;
        }

        int column = blueprint.column();
        int row = blueprint.row();

        // Check if position is within grid bounds
        if (column < 0 || column >= cols || row < 0 || row >= rows) {
            System.err.println("Invalid brick position: (" + column + "," + row + ") outside grid bounds [0-"
                    + (cols - 1) + ", 0-" + (rows - 1) + "]");
            return false;
        }

        // Check for duplicate position
        String positionKey = column + "," + row;
        if (addedPositions.contains(positionKey)) {
            System.err.println("Duplicate brick detected at position (" + column + "," + row + ")");
            return false;
        }

        // Mark position as occupied
        addedPositions.add(positionKey);
        return true;
    }

    /**
     * Factory method to create a brick from blueprint.
     */
    private Brick createBrick(LevelDefinition.BrickBlueprint blueprint,
            double brickWidth, double brickHeight, double gap, double horizontalPadding, double verticalPadding) {
        int column = blueprint.column();
        int row = blueprint.row();
        double x = horizontalPadding + column * (brickWidth + gap);
        double y = verticalPadding + row * (brickHeight + gap);
        int hitPoints = Math.max(1, blueprint.hitPoints());
        int scoreValue = 50 * hitPoints;

        return new Brick(x, y, brickWidth, brickHeight, hitPoints, scoreValue,
                column, row, blueprint.brickType(), blueprint.tags(), blueprint.modifiers());
    }

    @Override
    public void update(double deltaTime) {
        if (paused)
            return;

        InputManager input = context.getInput();
        if (input.isKeyJustPressed(KeyEvent.VK_ESCAPE)) {
            pauseGame();
            return;
        }

        if (gameOver) {
            handleGameOverInput(input);
            return;
        }

        updateGameplay(input, deltaTime);
        objectiveEngine.update(deltaTime);

        if (!gameOver && !stageCleared && isLevelComplete()) {
            handleLevelCompletion();
        }
    }

    /**
     * Factory method to update gameplay mechanics.
     */
    private void updateGameplay(InputManager input, double deltaTime) {
        handleMovementInput(input);
        paddle.update(deltaTime);

        // Clamp paddle within the neon dot border boundaries
        int width = context.getConfig().width();
        double leftBoundary = visualEffects.getLeftBound();
        double rightBoundary = visualEffects.getRightBound(width);
        paddle.clamp(leftBoundary, rightBoundary);

        if (awaitingLaunch) {
            attachBallToPaddle();
            if (input.isKeyJustPressed(KeyEvent.VK_SPACE))
                launchBall();
        } else {
            updateBalls(deltaTime);
        }

        paddleGunSystem.update(deltaTime, paddle, bricks, this::onBrickDestroyed);
        powerUpController.update(deltaTime, paddle, context.getConfig().height(), this::applyPowerUp);

        objectiveEngine.update(deltaTime);

        if (!gameOver && !stageCleared && isLevelComplete()) {
            handleLevelCompletion();
        }
    }

    @Override
    public void render(Graphics2D graphics) {
        Graphics2D g2 = (Graphics2D) graphics.create();
        try {
            int canvasWidth = context.getConfig().width();
            int canvasHeight = context.getConfig().height();

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // Draw background
            if (backgroundImage == null) {
                loadBackgroundImage();
            }
            if (backgroundImage != null) {
                g2.drawImage(backgroundImage, 0, 0, canvasWidth, canvasHeight, null);
            } else {
                g2.setColor(new Color(12, 16, 40));
                g2.fillRect(0, 0, canvasWidth, canvasHeight);
            }

            // Draw ARKANOID title and neon border dots
            visualEffects.drawGameAreaBorder(g2, canvasWidth, canvasHeight);

            // Render game objects (bricks, paddle, balls, power-ups)
            renderArena(g2);

            // Render side panels with score, lives, level, and objectives
            panelRenderer.render(g2, canvasWidth, canvasHeight, score, lives, activeLevel, objectiveEngine.snapshot());

            // Render center messages
            String message = null;
            if (paused) {
                message = localization.translate("gameplay.message.paused");
            } else if (!statusMessage.isBlank()) {
                message = statusMessage;
            }

            if (message != null && !message.isBlank()) {
                hudRenderer.renderCenterMessage(g2, message, canvasWidth, canvasHeight);
            } else if (awaitingLaunch && !gameOver && !paused) {
                renderLaunchPrompt(g2, canvasWidth, canvasHeight);
            }
        } finally {
            g2.dispose();
        }
    }

    private void renderArena(Graphics2D graphics) {
        // Render game objects
        for (Brick brick : bricks) {
            brick.render(graphics);
        }

        powerUpController.render(graphics);
        paddleGunSystem.render(graphics);

        if (paddle != null) {
            paddle.render(graphics);
        }

        for (Ball ball : balls) {
            ball.render(graphics);
        }
    }

    private void renderLaunchPrompt(Graphics2D graphics, int canvasWidth, int canvasHeight) {
        String prompt = localization.translate("gameplay.prompt.launch");
        if (prompt == null || prompt.isBlank()) {
            return;
        }
        Font previous = graphics.getFont();
        graphics.setFont(previous.deriveFont(Font.BOLD, 20f));
        graphics.setColor(new Color(255, 255, 255, 200));
        int textWidth = graphics.getFontMetrics().stringWidth(prompt);
        int x = (canvasWidth - textWidth) / 2;
        int y = canvasHeight - 36;
        graphics.drawString(prompt, x, y);
        graphics.setFont(previous);
    }

    private boolean isLevelComplete() {
        return objectiveEngine.arePrimaryObjectivesMet() || bricks.stream().allMatch(Brick::isDestroyed);
    }

    private void handleGameOverInput(InputManager input) {
        if (input.isKeyJustPressed(KeyEvent.VK_ENTER)) {
            startNewGame();
            return;
        }
        if (input.isKeyJustPressed(KeyEvent.VK_ESCAPE)) {
            context.getScenes().switchTo(ArkanoidGame.SCENE_MENU);
        }
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
        Ball primary = primaryBall();
        if (primary == null) {
            return;
        }
        primary.getPosition().x = paddle.getPosition().x + paddle.getWidth() / 2.0 - BALL_SIZE / 2.0;
        primary.getPosition().y = paddle.getPosition().y - BALL_SIZE - 4;
    }

    private void launchBall() {
        Ball primary = primaryBall();
        if (primary == null) {
            return;
        }
        awaitingLaunch = false;
        currentBallSpeed = BASE_BALL_SPEED;
        statusMessage = "";
        setBallVelocityByAngle(primary, Math.toRadians(random.nextDouble() * 120 - 60));
    }

    private void setBallVelocityByAngle(Ball ballRef, double angle) {
        if (ballRef == null) {
            return;
        }
        double vx = currentBallSpeed * Math.sin(angle);
        double vy = -Math.abs(currentBallSpeed * Math.cos(angle));
        ballRef.setVelocity(vx, vy);
    }

    private void updateBalls(double deltaTime) {
        List<Ball> snapshot = new ArrayList<>(balls);
        for (Ball ballRef : snapshot) {
            ballRef.update(deltaTime);
            if (constrainBallWithinArena(ballRef)) {
                continue;
            }
            handlePaddleCollision(ballRef);
            handleBrickCollisions(ballRef);
        }
        if (!pendingBalls.isEmpty()) {
            balls.addAll(pendingBalls);
            pendingBalls.clear();
        }
    }

    private boolean constrainBallWithinArena(Ball ballRef) {
        int width = context.getConfig().width();
        int height = context.getConfig().height();
        boolean hitBoundary = false;

        // Use visual effects boundaries (neon dot border)
        double leftBoundary = visualEffects.getLeftBound();
        double rightBoundary = visualEffects.getRightBound(width);
        double topBoundary = visualEffects.getTopBound();

        // Left boundary
        if (ballRef.getPosition().x <= leftBoundary) {
            ballRef.getPosition().x = leftBoundary;
            ballRef.invertX();
            hitBoundary = true;
        }
        // Right boundary
        else if (ballRef.getPosition().x + ballRef.getWidth() >= rightBoundary) {
            ballRef.getPosition().x = rightBoundary - ballRef.getWidth();
            ballRef.invertX();
            hitBoundary = true;
        }

        // Top boundary
        if (ballRef.getPosition().y <= topBoundary) {
            ballRef.getPosition().y = topBoundary;
            ballRef.invertY();
            hitBoundary = true;
        }

        // Bottom - lose life
        if (ballRef.getPosition().y > height) {
            balls.remove(ballRef);
            if (balls.isEmpty()) {
                loseLife();
            }
            return true;
        }

        if (hitBoundary) {
            normalizeBallSpeed(ballRef);
        }
        return false;
    }

    private void normalizeBallSpeed(Ball ballRef) {
        double vx = ballRef.getVelocity().x;
        double vy = ballRef.getVelocity().y;
        double length = Math.sqrt(vx * vx + vy * vy);
        if (length != 0) {
            double scale = currentBallSpeed / length;
            ballRef.setVelocity(vx * scale, vy * scale);
        }
    }

    private void handlePaddleCollision(Ball ballRef) {
        Rectangle2D ballBounds = ballRef.getBounds();
        Rectangle2D paddleBounds = paddle.getBounds();
        if (ballBounds.intersects(paddleBounds) && ballRef.getVelocity().y > 0) {
            double paddleCenter = paddleBounds.getCenterX();
            double ballCenter = ballBounds.getCenterX();
            double offset = (ballCenter - paddleCenter) / (paddle.getWidth() / 2.0);
            offset = Math.max(-1, Math.min(1, offset));
            double angle = Math.toRadians(60 * offset);
            currentBallSpeed = Math.min(560, currentBallSpeed * 1.02);
            setBallVelocityByAngle(ballRef, angle);
            ballRef.getPosition().y = paddle.getPosition().y - BALL_SIZE - 1;
        }
    }

    private void handleBrickCollisions(Ball ballRef) {
        Rectangle2D ballBounds = ballRef.getBounds();
        for (Brick brick : bricks) {
            if (brick.isDestroyed())
                continue;
            Rectangle2D brickBounds = brick.getBounds();
            if (brickBounds.intersects(ballBounds)) {
                Rectangle2D intersection = brickBounds.createIntersection(ballBounds);
                if (!ballRef.isFireActive()) {
                    if (intersection.getWidth() >= intersection.getHeight()) {
                        ballRef.invertY();
                    } else {
                        ballRef.invertX();
                    }
                }
                brick.hit();
                if (ballRef.isFireActive()) {
                    while (!brick.isDestroyed()) {
                        brick.hit();
                    }
                }
                if (brick.isDestroyed()) {
                    onBrickDestroyed(brick);
                }
                normalizeBallSpeed(ballRef);
                break;
            }
        }
    }

    private void onBrickDestroyed(Brick brick) {
        score += brick.getScoreValue();
        objectiveEngine.handleEvent(new ObjectiveEngine.ScoreAwardedEvent(brick.getScoreValue()));
        objectiveEngine.handleEvent(new ObjectiveEngine.BrickClearedEvent(
                brick.getGridColumn(),
                brick.getGridRow(),
                brick.getBlueprintType(),
                brick.getTags()));
        powerUpController.maybeSpawnFrom(brick);
    }

    private void applyPowerUp(PowerUp.Type type) {
        switch (type) {
            case EXPAND_PADDLE:
                paddle.setWidth(Math.min(paddle.getWidth() * 1.3, 240));
                break;
            case SLOW_BALL:
                slowBalls();
                break;
            case MULTI_BALL:
                spawnMultiBall();
                break;
            case FIRE_BALL:
                igniteFireBalls();
                break;
            case PADDLE_GUN:
                paddleGunSystem.activate();
                break;
            case EXTRA_LIFE:
                lives = Math.min(lives + 1, MAX_LIVES);
                break;
            default:
                break;
        }
    }

    private void slowBalls() {
        currentBallSpeed = Math.max(260, currentBallSpeed * 0.8);
        for (Ball ballRef : balls) {
            normalizeBallSpeed(ballRef);
        }
    }

    private void spawnMultiBall() {
        if (balls.isEmpty()) {
            return;
        }
        if (balls.size() >= MAX_SIMULTANEOUS_BALLS) {
            return;
        }
        Ball reference = primaryBall();
        if (reference == null) {
            return;
        }
        Ball cloneA = reference.duplicate();
        Ball cloneB = reference.duplicate();
        double baseAngle = Math.atan2(reference.getVelocity().y, reference.getVelocity().x);
        if (Double.isNaN(baseAngle) || Double.isInfinite(baseAngle)) {
            baseAngle = Math.toRadians(-45);
        }
        setBallVelocityByAngle(cloneA, baseAngle + Math.toRadians(25));
        setBallVelocityByAngle(cloneB, baseAngle - Math.toRadians(25));
        addExtraBall(cloneA);
        addExtraBall(cloneB);
    }

    private void igniteFireBalls() {
        for (Ball ballRef : balls) {
            ballRef.setFire(FIRE_BALL_DURATION_SECONDS);
        }
    }

    private void handleLevelCompletion() {
        if (stageCleared)
            return;

        stageCleared = true;
        awaitingLaunch = true;
        PlayerProfile profile = context.getProfileManager().getActiveProfile();

        updateProfileProgress(profile);

        if (levelManager.hasNext()) {
            advanceToNextLevel(profile);
        } else {
            completeAllLevels(profile);
        }
    }

    /**
     * Factory method to update profile progress.
     */
    private void updateProfileProgress(PlayerProfile profile) {
        profile.markLevelCompleted(activeLevel.id());
        profile.unlockLevel(activeLevel.id());
        context.getProfileManager().saveProfile();
    }

    /**
     * Factory method to advance to next level.
     */
    private void advanceToNextLevel(PlayerProfile profile) {
        levelManager.advance();
        LevelDefinition nextLevel = levelManager.current();
        profile.unlockLevel(nextLevel.id());
        profile.setCurrentLevelId(nextLevel.id());
        context.getProfileManager().saveProfile();
        loadLevel();
        statusMessage = localization.translate("gameplay.message.stageCleared");
    }

    /**
     * Factory method to handle game completion.
     */
    private void completeAllLevels(PlayerProfile profile) {
        profile.setCurrentLevelId(activeLevel.id());
        context.getProfileManager().saveProfile();
        gameOver = true;
        statusMessage = localization.translate("gameplay.message.victory");
    }

    private void loseLife() {
        lives--;
        paddleGunSystem.reset();
        pendingBalls.clear();
        if (lives <= 0) {
            balls.clear();
            gameOver = true;
            statusMessage = localization.translate("gameplay.message.gameOver");
            return;
        }
        statusMessage = localization.translate("gameplay.message.lifeLost");
        paddle.setWidth(BASE_PADDLE_WIDTH);
        currentBallSpeed = BASE_BALL_SPEED;
        PlayerProfile profile = context.getProfileManager().getActiveProfile();
        SkinCatalog.BallSkin ballSkin = SkinCatalog.ballSkin(profile.getActiveBallSkin());
        balls.clear();
        balls.add(createBall(ballSkin));
        resetBall();
    }

    private class SceneObjectiveListener implements ObjectiveEngine.Listener {
        @Override
        public void onObjectiveProgress(ObjectiveEngine.ObjectiveState state) {
        }

        @Override
        public void onObjectiveCompleted(ObjectiveEngine.ObjectiveState state) {
            statusMessage = summarizeObjective("hud.objectiveIndicator.completed", state);
        }

        @Override
        public void onObjectiveFailed(ObjectiveEngine.ObjectiveState state) {
            statusMessage = summarizeObjective("hud.objectiveIndicator.failed", state);
        }

        private String summarizeObjective(String indicatorKey, ObjectiveEngine.ObjectiveState state) {
            String indicator = localization.translate(indicatorKey);
            String labelKey = "objective." + state.id();
            String label = localization.translate(labelKey);
            if (label.equals(labelKey)) {
                label = state.id();
            }
            return indicator + " " + label;
        }
    }

}
