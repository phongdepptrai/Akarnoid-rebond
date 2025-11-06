package com.arcade.arkanoid.gameplay;

import com.arcade.arkanoid.ArkanoidGame;
import com.arcade.arkanoid.engine.assets.AssetManager;
import com.arcade.arkanoid.engine.core.GameContext;
import com.arcade.arkanoid.engine.input.InputManager;
import com.arcade.arkanoid.engine.scene.Scene;
import com.arcade.arkanoid.engine.util.NeonGlowEffect;
import com.arcade.arkanoid.engine.util.VisualEffect;
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
import com.arcade.arkanoid.gameplay.system.HudRenderer;
import com.arcade.arkanoid.localization.LocalizationService;
import com.arcade.arkanoid.menu.PauseScene;
import com.arcade.arkanoid.profile.PlayerProfile;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
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

    // Frame/Panel constants
    public static final int SIDE_PANEL_WIDTH = GameplayPanelRenderer.getPanelWidth();

    private final LevelManager levelManager = new LevelManager();
    private final HudRenderer hudRenderer;
    private final GameplayPanelRenderer panelRenderer = GameplayPanelRenderer.getInstance();
    private final Random random = new Random();
    
    // Visual effects
    private final VisualEffect neonDotEffect;
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
    private BufferedImage paddleImage;
    private BufferedImage backgroundImage;

    public GameplayScene(GameContext context) {
        super(context);
        this.localization = context.getLocalizationService();
        this.hudRenderer = new HudRenderer(localization);
        
        // Initialize neon glow effect for border dots
        this.neonDotEffect = new NeonGlowEffect.Builder()
                .size(2)
                .coreColor(new Color(0, 255, 255))
                .glowColor(new Color(0, 200, 255))
                .glowLayers(3)
                .intensity(1.0f)
                .pulsing(false)
                .build();
    }

    @Override
    public void onEnter() {
        loadPaddleImage();
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
        powerUps.clear();
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
                BASE_PADDLE_WIDTH, PADDLE_HEIGHT, PADDLE_SPEED,
                paddleSkin.fillColor(), paddleSkin.borderColor());
        paddle.setPaddleImage(paddleImage);

        ball = new Ball(0, 0, BALL_SIZE, ballSkin.fillColor(), ballSkin.borderColor());
        currentBallSpeed = BASE_BALL_SPEED;
        resetBall();
    }

    private void resetBall() {
        awaitingLaunch = true;
        ball.resetPosition(
                paddle.getPosition().x + paddle.getWidth() / 2.0 - BALL_SIZE / 2.0,
                paddle.getPosition().y - BALL_SIZE - 4);
        currentBallSpeed = Math.max(260, currentBallSpeed);
        ball.setVelocity(0, 0);
    }

    private void buildBricks(LevelDefinition definition) {
        int cols = definition.columns();
        int rows = definition.rows();
        if (cols <= 0 || rows <= 0)
            return;

        double horizontalPadding = SIDE_PANEL_WIDTH + 30;
        double verticalPadding = 90;
        double gap = 4;
        double availableWidth = context.getConfig().width() - horizontalPadding * 2;
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
     * Factory method to validate brick position and prevent duplicates.
     */
    private boolean isValidBrickPosition(LevelDefinition.BrickBlueprint blueprint,
            int cols, int rows, java.util.Set<String> addedPositions) {
        int column = blueprint.column();
        int row = blueprint.row();

        if (column < 0 || column >= cols || row < 0 || row >= rows)
            return false;

        String position = column + "," + row;
        if (addedPositions.contains(position)) {
            System.err.println("Skipping duplicate brick at (" + column + "," + row + ")");
            return false;
        }
        addedPositions.add(position);
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
        paddle.clamp(SIDE_PANEL_WIDTH, context.getConfig().width() - SIDE_PANEL_WIDTH);

        if (awaitingLaunch) {
            attachBallToPaddle();
            if (input.isKeyJustPressed(KeyEvent.VK_SPACE))
                launchBall();
        } else {
            ball.update(deltaTime);
            constrainBallWithinArena();
            handlePaddleCollision();
            handleBrickCollisions();
        }

        updatePowerUps(deltaTime);
    }

    private boolean isLevelComplete() {
        return objectiveEngine.arePrimaryObjectivesMet() || bricks.stream().allMatch(Brick::isDestroyed);
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
        if (handleBoundaryCollisions() && ball.getPosition().y <= context.getConfig().height()) {
            normalizeBallSpeed();
        }
    }

    /**
     * Factory method for handling ball boundary collisions.
     * 
     * @return true if ball hit any boundary, false otherwise
     */
    private boolean handleBoundaryCollisions() {
        double width = context.getConfig().width();
        double height = context.getConfig().height();
        boolean hitBoundary = false;

        // Left boundary
        if (ball.getPosition().x <= SIDE_PANEL_WIDTH) {
            ball.getPosition().x = SIDE_PANEL_WIDTH;
            ball.invertX();
            hitBoundary = true;
        }
        // Right boundary
        else if (ball.getPosition().x + ball.getWidth() >= width - SIDE_PANEL_WIDTH) {
            ball.getPosition().x = width - SIDE_PANEL_WIDTH - ball.getWidth();
            ball.invertX();
            hitBoundary = true;
        }

        // Top boundary
        if (ball.getPosition().y <= 50) {
            ball.getPosition().y = 50;
            ball.invertY();
            hitBoundary = true;
        }

        // Bottom - lose life
        if (ball.getPosition().y > height) {
            loseLife();
        }

        return hitBoundary;
    }

    private void normalizeBallSpeed() {
        double vx = ball.getVelocity().x;
        double vy = ball.getVelocity().y;
        double length = Math.sqrt(vx * vx + vy * vy);
        if (length != 0) {
            double scale = currentBallSpeed / length;
            ball.setVelocity(vx * scale, vy * scale);
        }
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
            if (brick.isDestroyed())
                continue;

            if (brick.getBounds().intersects(ballBounds)) {
                handleBrickHit(brick, ballBounds);
                break;
            }
        }
    }

    /**
     * Factory method to handle brick collision logic.
     */
    private void handleBrickHit(Brick brick, Rectangle2D ballBounds) {
        Rectangle2D intersection = brick.getBounds().createIntersection(ballBounds);
        if (intersection.getWidth() >= intersection.getHeight()) {
            ball.invertY();
        } else {
            ball.invertX();
        }

        brick.hit();
        if (brick.isDestroyed()) {
            processBrickDestruction(brick);
        }
        normalizeBallSpeed();
    }

    /**
     * Factory method to process brick destruction events.
     */
    private void processBrickDestruction(Brick brick) {
        score += brick.getScoreValue();
        objectiveEngine.handleEvent(new ObjectiveEngine.ScoreAwardedEvent(brick.getScoreValue()));
        objectiveEngine.handleEvent(new ObjectiveEngine.BrickClearedEvent(
                brick.getGridColumn(), brick.getGridRow(),
                brick.getBlueprintType(), brick.getTags()));
        maybeSpawnPowerUp(brick);
    }

    private void maybeSpawnPowerUp(Brick brick) {
        if (random.nextDouble() > POWERUP_DROP_CHANCE)
            return;

        PowerUp.Type type = random.nextBoolean() ? PowerUp.Type.EXPAND_PADDLE : PowerUp.Type.SLOW_BALL;
        PowerUp powerUp = createPowerUp(brick, type);
        powerUps.add(powerUp);
    }

    /**
     * Factory method for creating power-ups from brick.
     */
    private PowerUp createPowerUp(Brick brick, PowerUp.Type type) {
        double x = brick.getPosition().x + brick.getWidth() / 2.0 - POWERUP_SIZE / 2.0;
        double y = brick.getPosition().y + brick.getHeight() / 2.0 - POWERUP_SIZE / 2.0;
        Color color = type == PowerUp.Type.EXPAND_PADDLE ? new Color(0x8BC34A) : new Color(0xFFEB3B);
        return new PowerUp(x, y, POWERUP_SIZE, type, color);
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
        drawGameAreaBorder(graphics);
        renderGameElements(graphics);
        renderStatusMessage(graphics);
    }

    /**
     * Factory method to draw neon border around game play area only.
     */
    private void drawGameAreaBorder(Graphics2D graphics) {
        int width = context.getConfig().width();
        int height = context.getConfig().height();

        // Game area boundaries (between side panels)
        int leftBound = SIDE_PANEL_WIDTH;
        int rightBound = width - SIDE_PANEL_WIDTH;
        int topBound = 50;

        int spacing = 8;

        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Top border
        for (int x = leftBound; x <= rightBound; x += spacing) {
            neonDotEffect.render(graphics, x, topBound);
        }

        // Left border (only in game area)
        for (int y = topBound; y <= height - 60; y += spacing) {
            neonDotEffect.render(graphics, leftBound, y);
        }

        // Right border (only in game area)
        for (int y = topBound; y <= height - 60; y += spacing) {
            drawNeonDot(graphics, rightBound, y, dotSize, neonCyan, neonGlow);
        }
    }

    /**
     * Factory method to render all game elements.
     */
    private void renderGameElements(Graphics2D graphics) {
        int width = context.getConfig().width();
        int height = context.getConfig().height();
        panelRenderer.render(graphics, width, height, score, lives, activeLevel, objectiveEngine.snapshot());

        bricks.forEach(brick -> brick.render(graphics));
        paddle.render(graphics);
        ball.render(graphics);
        powerUps.forEach(powerUp -> powerUp.render(graphics));
    }

    /**
     * Factory method to render status messages.
     */
    private void renderStatusMessage(Graphics2D graphics) {
        if (!paused && (awaitingLaunch || gameOver || !statusMessage.isEmpty())) {
            String message = statusMessage.isEmpty() ? localization.translate("gameplay.prompt.launch") : statusMessage;
            hudRenderer.renderCenterMessage(graphics, message,
                    context.getConfig().width(), context.getConfig().height());
        }
    }

    private void drawBackground(Graphics2D graphics) {
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw background image if available
        if (backgroundImage != null) {
            int width = context.getConfig().width();
            int height = context.getConfig().height();
            graphics.drawImage(backgroundImage, 0, 0, width, height, null);
        } else {
            // Fallback to solid color
            graphics.setColor(new Color(18, 18, 30));
            graphics.fillRect(0, 0, context.getConfig().width(), context.getConfig().height());
        }
    }

    /**
     * Factory method to draw a single neon dot with multi-layer glow effect.
     */
    private void drawNeonDot(Graphics2D g, int x, int y, int size, Color core, Color glow) {
        // Enable anti-aliasing for smooth circles
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Outer glow layer (largest, most transparent)
        g.setColor(new Color(0, 200, 255, 60));
        g.fillOval(x - size * 3, y - size * 3, size * 6, size * 6);
        
        // Middle glow layer
        g.setColor(new Color(0, 220, 255, 100));
        g.fillOval(x - size * 2, y - size * 2, size * 4, size * 4);
        
        // Inner glow layer
        g.setColor(glow);
        g.fillOval(x - size, y - size, size * 2, size * 2);

        // Core dot (bright, solid)
        g.setColor(core);
        g.fillOval(x - size / 2, y - size / 2, size, size);
        
        // Extra bright center point
        g.setColor(Color.WHITE);
        g.fillOval(x - 1, y - 1, 2, 2);
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