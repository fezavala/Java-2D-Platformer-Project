package view;

import model.*;
import util.Vector2D;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.HashMap;

// The GamePanel class draws images onto the screen based on the GameManagers state
// Draws the player, tiles, and menus

// NOTE: This class is essentially an all-in-one class for rendering, with the HUD as an exception
public class GamePanel extends JPanel {

    // Screen and Rendering Constants
    private static final int SCREEN_WIDTH = 1856;
    private static final int SCREEN_HEIGHT = 960;
    private static final int SCALE = 2;
    private static final Vector2D RENDER_SIZE = new Vector2D((double) SCREEN_WIDTH / SCALE, (double) SCREEN_HEIGHT / SCALE);
    private static final Color BACKGROUND_COLOR = new Color(0, 191, 255);
    private static final double CAMERA_DRAG = 10;

    // Player Rendering Constants
    private static final int PLAYER_SPRITE_WIDTH_OFFSET = 2;
    private static final int PLAYER_SCALE = 2;
    private static final double PLAYER_IMAGE_WIDTH = 33;
    private static final Vector2D PLAYER_SPRITE_POSITION_OFFSET = new Vector2D(-18.0, -28.0);

    // Load paths for images
    private static final String IMAGE_PATH = "/sprites/";
    private static final String TILE_PATH = IMAGE_PATH + "tiles/";
    private static final String PLAYER_IMAGE_PATH = IMAGE_PATH + "player/";
    private static final String PLAYER_IDLE_IMAGE_PATH = PLAYER_IMAGE_PATH + "idle/";
    private static final String PLAYER_RUN_IMAGE_PATH = PLAYER_IMAGE_PATH + "run/";
    private static final String PLAYER_JUMP_UP_IMAGE_PATH = PLAYER_IMAGE_PATH + "jump/jump_up.png";
    private static final String PLAYER_JUMP_DOWN_IMAGE_PATH = PLAYER_IMAGE_PATH + "jump/jump_down.png";
    private static final String PLAYER_HURT_IMAGE_PATH = PLAYER_IMAGE_PATH + "hurt/";
    private static final String GRASS_TILE_PATH = TILE_PATH + "ground/";
    private static final String GOAL_TILE_PATH = TILE_PATH + "goal/goal.png";
    private static final String SPIKES_TILE_PATH = TILE_PATH + "spikes/";
    private static final String COLLECTIBLE_TILE_PATH = TILE_PATH + "collectible/";
    private static final String JAVA_LOGO_PATH = IMAGE_PATH + "java_logo.png";

    // GameManager is stored for ease of access of the games current state
    private GameManager gameManager;

    // HashMap that stores all the tile images based on their orientation and type
    private final HashMap<TileType, HashMap<TileOrientation, ImageProvider>> tileSprites = new HashMap<>();

    private final HashMap<PlayerState, ImageProvider> playerImages = new HashMap<>();
    private ImageProvider currentPlayerVisual;
    private AffineTransformOp playerImageFlipper;
    private boolean playerFlipped = false;

    private SpriteAnimation collectibleAnimation;

    private int spriteSize;

    // Camera related fields
    private final Vector2D cameraPosition = new Vector2D(0, 0);
    private Vector2D previousPlayerPosition = new Vector2D(0, 0);

    // Circle transition animation fields
    private double circleTransitionAnimationProgress = 0;
    private boolean flipCircleTransitionProgress = false;
    private boolean circleTransitionAnimationComplete = true;
    private static final double CIRCLE_TRANSITION_ANIMATION_PROGRESS_PER_FRAME = 0.03;
    private static final double MAX_CIRCLE_TRANSITION_PROGRESS = 35;

    private final GameHUD gameHUD;

    public GamePanel() {
        loadPlayerSprites();
        loadTileSprites();
        createImageFlipper();
        Sprite javaLogoSprite = new Sprite(JAVA_LOGO_PATH);
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(BACKGROUND_COLOR);
        this.setDoubleBuffered(true);
        this.setFocusable(true);  // Lets the panel be focused on, allowing inputs to go through
        gameHUD = new GameHUD(collectibleAnimation, javaLogoSprite);
    }

    public void setGameManager(GameManager gameManager) {
        this.gameManager = gameManager;
        // Also set the sprite size here
        this.spriteSize = gameManager.getTileSize();
    }

    private void createImageFlipper() {
        AffineTransform transform = new AffineTransform();
        transform.translate(PLAYER_IMAGE_WIDTH, 0);
        transform.scale(-1d, 1d);
        playerImageFlipper = new AffineTransformOp(transform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
    }

    private void loadPlayerSprites() {
        for (PlayerState c : PlayerState.values()) {
            //Loading 4 idle sprites
            if (c == PlayerState.IDLE) {
                int spriteAmount = 4;
                Sprite[] sprites = new Sprite[spriteAmount];
                for (int i = 0; i < spriteAmount; i++) {
                    // Files were meant to start from 0, but it duplicated the first image so they now start at one, thus the +1 magic number
                    sprites[i] = new Sprite(PLAYER_IDLE_IMAGE_PATH + (i + 1) + ".png");
                }
                SpriteAnimation playerIdle = new SpriteAnimation(sprites, 5);
                playerImages.put(c, playerIdle);
//                persistantAnimations[0] = playerIdle;

            }
            if (c == PlayerState.RUN) {
                int spriteAmount = 6;
                Sprite[] sprites = new Sprite[spriteAmount];
                for (int i = 0; i < spriteAmount; i++) {
                    sprites[i] = new Sprite(PLAYER_RUN_IMAGE_PATH + (i + 1) + ".png");
                }
                SpriteAnimation playerRun = new SpriteAnimation(sprites, 5);
                playerImages.put(c, playerRun);
//                persistantAnimations[1] = playerRun;
            }
            if (c == PlayerState.JUMP_UP) {
                Sprite jump_up = new Sprite(PLAYER_JUMP_UP_IMAGE_PATH);
                playerImages.put(c, jump_up);
            }
            if (c == PlayerState.JUMP_DOWN) {
                Sprite jump_down = new Sprite(PLAYER_JUMP_DOWN_IMAGE_PATH);
                playerImages.put(c, jump_down);
            }
            if (c == PlayerState.DEATH) {
                // Place Hurt Sprites here
                int spriteAmount = 2;
                Sprite[] sprites = new Sprite[spriteAmount];
                for (int i = 0; i < spriteAmount; i++) {
                    sprites[i] = new Sprite(PLAYER_HURT_IMAGE_PATH + (i + 1) + ".png");
                }
                SpriteAnimation playerDeath = new SpriteAnimation(sprites, 5);
                playerImages.put(c, playerDeath);
            }
        }
        currentPlayerVisual = playerImages.get(PlayerState.IDLE);
    }

    private void loadTileSprites() {
        for (TileType t : TileType.values()) {
            if (t == TileType.GROUND) {
                HashMap<TileOrientation, ImageProvider> grassSprites = new HashMap<>();
                int index = 0;
                for (TileOrientation o : TileOrientation.values()) {
                    Sprite tileSprite = new Sprite(GRASS_TILE_PATH + (index + 1) + ".png");
                    grassSprites.put(o, tileSprite);
                    index++;
                }
                tileSprites.put(t, grassSprites);
            }
            if (t == TileType.GOAL) {
                HashMap<TileOrientation, ImageProvider> goalSprites = new HashMap<>();
                Sprite goalSprite = new Sprite(GOAL_TILE_PATH);
                goalSprites.put(TileOrientation.TOP_LEFT, goalSprite);
                tileSprites.put(t, goalSprites);
            }
            if (t == TileType.HAZARD) {
                HashMap<TileOrientation, ImageProvider> spikeSprites = new HashMap<>();
                for (int i = 0; i < 4; i++) {
                    Sprite spikeSprite = new Sprite(SPIKES_TILE_PATH + (i + 1) + ".png");
                    spikeSprites.put(TileOrientation.getOrientationFromInt(i), spikeSprite);
                }
                tileSprites.put(t, spikeSprites);
            }
            if (t == TileType.COLLECTIBLE) {
                HashMap<TileOrientation, ImageProvider> collectibleSprites = new HashMap<>();
                int spriteAmount = 12;
                Sprite[] sprites = new Sprite[spriteAmount];
                for (int i = 0; i < spriteAmount; i++) {
                    sprites[i] = new Sprite(COLLECTIBLE_TILE_PATH + (i + 1) + ".png");
                }
                collectibleAnimation = new SpriteAnimation(sprites, 5);
                collectibleSprites.put(TileOrientation.TOP_LEFT, collectibleAnimation);
                tileSprites.put(t, collectibleSprites);
            }
        }
    }

    // Renders tiles that are visible on screen
    private void renderTiles(Graphics2D g2d, Vector2D cameraPosition) {
        // Grab tiles to be rendered:
        TileMap.Tile[] visibleTiles = gameManager.getVisibleTiles(cameraPosition, RENDER_SIZE);
        // Render each tile depending on what type of tile it is
        for (TileMap.Tile tile : visibleTiles) {
            if (tile == null) continue;
            BufferedImage tileImage = tileSprites.get(tile.getType()).get(tile.getOrientation()).getActiveImage();
            Vector2D tilePos = tile.getWorldPosition();
            g2d.drawImage(tileImage, (int) (tilePos.x - cameraPosition.x), (int) (tilePos.y - cameraPosition.y), spriteSize, spriteSize, null);
        }
    }

    // Handles the state of the level transition animation
    private void handleLevelTransition(Graphics2D g2) {
        // Start level transition
        if (gameManager.getGameState() == GameState.LEVEL_TRANSITION && circleTransitionAnimationComplete) {
            circleTransitionAnimationComplete = false;
            circleTransitionAnimationProgress = 0;
            flipCircleTransitionProgress = false;
        }

        // Process circleTransitionAnimation
        if (!circleTransitionAnimationComplete) {
            circleTransitionAnimation(g2);
        }

        // Check for the animations state
        if (flipCircleTransitionProgress) {
            circleTransitionAnimationProgress = Math.max(0, circleTransitionAnimationProgress - CIRCLE_TRANSITION_ANIMATION_PROGRESS_PER_FRAME);
        } else {
            circleTransitionAnimationProgress = Math.min(1, circleTransitionAnimationProgress + CIRCLE_TRANSITION_ANIMATION_PROGRESS_PER_FRAME);
        }
        if (circleTransitionAnimationProgress >= 1) {
            flipCircleTransitionProgress = true;
        }
        if (circleTransitionAnimationProgress <= 0) {
            circleTransitionAnimationComplete = true;
        }
    }


    private void renderPlayer(Graphics2D g2d, Vector2D cameraPosition, PlayerState currentPlayerState) {
        // Check and change currently player visual
        if (currentPlayerVisual != playerImages.get(currentPlayerState)) {
            if (currentPlayerVisual instanceof SpriteAnimation) {
                ((SpriteAnimation) currentPlayerVisual).resetAnimation();
            }
            currentPlayerVisual = playerImages.get(currentPlayerState);
        }

        // Update Player animation (if needed)
        if (currentPlayerVisual instanceof SpriteAnimation) {
            ((SpriteAnimation) currentPlayerVisual).updateAnimation();
        }

        // Check if the player is flipped
        double playerHorizontalVelocity = gameManager.getPlayerHorizontalVelocity();
        if (playerHorizontalVelocity < 0.0) {
            playerFlipped = true;
        }
        if (playerHorizontalVelocity > 0.0) {
            playerFlipped = false;
        }

        // Set player sprite and flip it if needed
        BufferedImage currentPlayerImage = playerFlipped ?
                playerImageFlipper.filter(currentPlayerVisual.getActiveImage(), null) :
                currentPlayerVisual.getActiveImage();

        // Set the position of the player on the screen
        Vector2D playerWorldPosition = gameManager.getPlayerWorldPosition();
        Vector2D playerScreenPosition = new Vector2D(
                playerWorldPosition.x + PLAYER_SPRITE_POSITION_OFFSET.x - cameraPosition.x,
                playerWorldPosition.y + PLAYER_SPRITE_POSITION_OFFSET.y - cameraPosition.y);

        // Render the player
        // Player images are 33x32 for some reason, so the width offset is applied to manage this
        g2d.drawImage(currentPlayerImage,
                (int) (playerScreenPosition.x),
                (int) (playerScreenPosition.y),
                PLAYER_SCALE * (spriteSize) + PLAYER_SPRITE_WIDTH_OFFSET,
                PLAYER_SCALE * spriteSize,
                null);
    }

    // If circleTransitionAnimationComplete == false, this animation plays in a fire and forget manner
    private void circleTransitionAnimation(Graphics2D g2d) {
        g2d.setColor(Color.BLACK);
        double progressSize = Math.pow(MAX_CIRCLE_TRANSITION_PROGRESS * circleTransitionAnimationProgress, 2);
        double xValue = (RENDER_SIZE.x / 2) - progressSize / 2;
        double yValue = (RENDER_SIZE.y / 2) - progressSize / 2;
        g2d.fillOval((int) xValue, (int) yValue, (int) progressSize, (int) progressSize);
    }

    // Called from GamePanel.repaint() 60 times per second to render everything visible on screen
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.scale(SCALE, SCALE);  // Scale the game up to make it fill out the screen

        collectibleAnimation.updateAnimation();

        // If in the main menu, do not process anything else
        if (gameManager.getGameState() == GameState.MAIN_MENU || !gameManager.getTileMapActive()) {
            gameHUD.drawMainMenu(g2);
            handleLevelTransition(g2);
            g2.dispose();
            return;
        }

        // Update camera based on players position and state
        PlayerState currentPlayerState = gameManager.getPlayerState();
        Vector2D playerCenter = gameManager.getPlayerCenterPosition();
        if (currentPlayerState != PlayerState.DEATH) previousPlayerPosition = playerCenter;
        Vector2D usePosition = currentPlayerState == PlayerState.DEATH ? previousPlayerPosition : playerCenter;
        cameraPosition.x += (usePosition.x - RENDER_SIZE.x / 2 - cameraPosition.x) / CAMERA_DRAG;
        cameraPosition.y += (usePosition.y - RENDER_SIZE.y / 2 - cameraPosition.y) / CAMERA_DRAG;
        // Prevents sprite jitter at the expense of camera smoothness:
        Vector2D pixelAlignedCameraPosition = new Vector2D((int) cameraPosition.x, (int) cameraPosition.y);

        // The main rendering order:
        // Order matters, whatever is rendered first will be overwritten by whatever is rendered on top of it
        renderTiles(g2, pixelAlignedCameraPosition);
        renderPlayer(g2, pixelAlignedCameraPosition, currentPlayerState);
        // Render appropriate UI depending on the game state
        switch (gameManager.getGameState()) {
            case IN_LEVEL:
                gameHUD.drawGameHUD(g2, gameManager.getCollectibleAmount(), gameManager.getLevelTime());
                break;
            case LEVEL_FINISHED:
                gameHUD.drawLevelCompleteMenu(g2,
                        gameManager.getCurrentLevel(),
                        gameManager.getCollectibleAmount(),
                        gameManager.getLevelTime(),
                        gameManager.getSavedLevelCollectibleCount(),
                        gameManager.getSavedLevelTime(),
                        gameManager.isLevelCollectibleRecord(),
                        gameManager.isLevelTimeRecord());
                break;
        }
        handleLevelTransition(g2);

        g2.dispose();  // Once drawing is done, release the resources it uses
    }
}

