package model;

import util.InputListener;
import util.Vector2D;

// The main player class of the game.
// move() is called by the GameManager every frame to update the players position
// Dependency Injection with the TileMap is used frequently due to how closely these 2 classes need to work together
public class Player extends MovingObject implements InputListener {

    private static final double GRAVITY = 0.3;  // Constantly applied to player at all times
    private static final double ACCELERATION = 0.3;
    private static final double MAX_HORIZONTAL_SPEED = 7;
    private static final double MAX_VERTICAL_SPEED = 15;
    private static final double JUMP_VELOCITY = -8;

    private static final int PLAYER_WIDTH = 14 * 2;
    private static final int PLAYER_HEIGHT = 18 * 2;

    private static final TileType GROUND_TILES = TileType.GROUND;
    private static final TileType GOAL_TILES = TileType.GOAL;
    private static final TileType HAZARD_TILES = TileType.HAZARD;

    // Observer that sends events to the GameManager to process
    private final EventObserver observer = new EventObserver();

    private boolean onGround = false;

    private boolean dead = false;
    private int playerDeathTimer = 0;
    private static final int DEATH_TIMEOUT = 90;
    private boolean disabled = false;

    // Controls
    private Vector2D moveDirection = new Vector2D();
    private boolean jumpPressed = false;

    public Player(EventListener eventListener) {
        initializePlayerDefaults();
        observer.addListener(eventListener);
    }

    public Player(EventListener eventListener, Vector2D worldPosition) {
        initializePlayerDefaults();
        this.worldPosition = worldPosition;
        observer.addListener(eventListener);
    }

    // GameObject default values for player
    private void initializePlayerDefaults() {
        this.width = PLAYER_WIDTH;
        this.height = PLAYER_HEIGHT;
        this.hitboxOffset = new Vector2D();
    }

    private void handleHorizontalVelocity() {
        // Accelerate in the direction of input, else decelerate to 0.
        if (moveDirection.x != 0.0) {
            velocity.x = velocity.x + ACCELERATION * moveDirection.x;
        } else {
            if (velocity.x < 0.0) {
                velocity.x = Math.min(velocity.x + ACCELERATION, 0.0);
            }
            if (velocity.x > 0.0) {
                velocity.x = Math.max(velocity.x - ACCELERATION, 0.0);
            }
        }
    }

    // Constantly apply gravity and handle jump press
    private void handleVerticalVelocity() {
        velocity.y += GRAVITY;
        if (onGround && jumpPressed) {
            velocity.y += JUMP_VELOCITY;
        }
    }

    // Main method for collision detection, x and y movement is separated and processed separately
    private void moveAndCollide(TileMap tileMap) {
        // Handling one movement axis at a time
        // x-axis:
        if (this.velocity.x != 0.0) {
            worldPosition.x += velocity.x;  // Update position
            RectangleBox entityHitBox = this.getHitBox();  // Get hitbox based on updated position
            RectangleBox[] tileHitBoxes = tileMap.getSurroundingTileHitBoxes(entityHitBox.getCenter(), GROUND_TILES);
            for (RectangleBox tileHitBox : tileHitBoxes) {  // Use updated hitbox to check collisions
                if (tileHitBox == null) continue;
                if (entityHitBox.intersects(tileHitBox)) {
                    Vector2D newPlayerPosition = new Vector2D(worldPosition);
                    // If we move right and collide with a tile
                    if (velocity.x > 0.0) {
                        // Snap the entities position to the left edge of the tile
                        newPlayerPosition.x = tileHitBox.getLeftSide() - this.width - hitboxOffset.x;
                    }
                    // If we move left and collide with a tile
                    if (velocity.x < 0.0) {
                        // Snap the entities position to the right edge of the tile
                        newPlayerPosition.x = tileHitBox.getRightSide() - hitboxOffset.x;
                    }
                    this.velocity.x = 0.0;  // Reset x velocity, the player has hit a wall
                    this.worldPosition.x = newPlayerPosition.x;  // Finalize player position
                }
            }
        }
        // y-axis:
        if (this.velocity.y != 0.0) {
            worldPosition.y += velocity.y;
            RectangleBox entityHitBox = this.getHitBox();
            RectangleBox[] tileHitBoxes = tileMap.getSurroundingTileHitBoxes(entityHitBox.getCenter(), GROUND_TILES);
            for (RectangleBox tileHitBox : tileHitBoxes) {
                if (tileHitBox == null) continue;
                if (entityHitBox.intersects(tileHitBox)) {
                    Vector2D newPlayerPosition = new Vector2D(worldPosition);
                    // If we move down and collide with a tile
                    if (velocity.y > 0.0) {
                        // Snap the entities position to the top edge of the tile
                        newPlayerPosition.y = tileHitBox.getTopSide() - this.height - hitboxOffset.y;
                        onGround = true;
                    }
                    // If we move up and collide with a tile
                    if (velocity.y < 0.0) {
                        // Snap the entities position to the bottom edge of the tile
                        newPlayerPosition.y = tileHitBox.getBottom() - hitboxOffset.y;
                    }
                    this.velocity.y = 0.0;  // Reset y velocity, the player has hit a floor/ceiling
                    this.worldPosition.y = newPlayerPosition.y;  // Finalize player position
                }
            }
        }
    }

    private void checkGoal(TileMap tileMap) {
        if (collideWithTile(tileMap, GOAL_TILES)) {
            observer.notifyListeners(GameEvent.GOAL_REACHED);
            velocity = new Vector2D();
        }
    }

    private void checkHazards(TileMap tileMap) {
        if (collideWithTile(tileMap, HAZARD_TILES)) {
            velocity = new Vector2D(0, JUMP_VELOCITY);
            dead = true;
        }
    }

    private void checkCollectibles(TileMap tileMap) {
        RectangleBox entityHitBox = this.getHitBox();
        RectangleBox[] tileHitBoxes = tileMap.getSurroundingTileHitBoxes(entityHitBox.getCenter(), TileType.COLLECTIBLE);
        for (RectangleBox tileHitBox : tileHitBoxes) {
            if (tileHitBox == null) continue;
            if (entityHitBox.intersects(tileHitBox)) {
                tileMap.disableTile(tileHitBox.getCenter(), TileType.COLLECTIBLE);
                observer.notifyListeners(GameEvent.COLLECTIBLE);
            }
        }
    }

    private boolean collideWithTile(TileMap tileMap, TileType tileType) {
        RectangleBox entityHitBox = this.getHitBox();
        RectangleBox[] tileHitBoxes = tileMap.getSurroundingTileHitBoxes(entityHitBox.getCenter(), tileType);
        for (RectangleBox tileHitBox : tileHitBoxes) {
            if (tileHitBox == null) continue;
            if (entityHitBox.intersects(tileHitBox)) {
                return true;
            }
        }
        return false;
    }

    // Player class goes through a mini death animation for DEATH_TIMEOUT duration
    private void processDeathState() {
        playerDeathTimer++;
        moveDirection = new Vector2D();
        jumpPressed = false;
        handleVerticalVelocity();
        velocity.y = Math.clamp(velocity.y, -MAX_VERTICAL_SPEED, MAX_VERTICAL_SPEED);
        this.worldPosition.y += velocity.y;
        if (playerDeathTimer >= DEATH_TIMEOUT) {
            dead = true;
            observer.notifyListeners(GameEvent.DAMAGE);
        }
    }

    // This method is called after move, so onGround can be true and useful
    public PlayerState getCurrentPlayerState() {
        if (disabled || dead) return PlayerState.DEATH;
        if (!onGround) {
            if (velocity.y < 0.0) {
                return PlayerState.JUMP_UP;
            }
            return PlayerState.JUMP_DOWN;
        }
        if (velocity.x != 0.0) {
            return PlayerState.RUN;
        }
        return PlayerState.IDLE;
    }

    public Vector2D getPlayerVelocity() {
        return velocity;
    }

    public Vector2D getPlayerCenterPosition() {
        return getHitBox().getCenter();
    }

    public void resetPlayer(Vector2D newWorldPosition) {
        this.worldPosition = newWorldPosition;
        this.velocity = new Vector2D();
        playerDeathTimer = 0;
        dead = false;
        disabled = false;
    }

    // Updates the player position and state depending on user input routed here with the routeInput method
    @Override
    public void move(TileMap tileMap) {
        if (disabled) return;
        // When dead, player input is disabled and a death "animation" plays by forcing a jump w/o collisions
        if (dead) {
            processDeathState();
            return;
        }

        handleHorizontalVelocity();
        handleVerticalVelocity();
        onGround = false;

        // Make velocity not infinitely big/small
        velocity.x = Math.clamp(velocity.x, -MAX_HORIZONTAL_SPEED, MAX_HORIZONTAL_SPEED);
        velocity.y = Math.clamp(velocity.y, -MAX_VERTICAL_SPEED, MAX_VERTICAL_SPEED);

        // Move object with collision detection
        moveAndCollide(tileMap);
        // Check for not solid objects
        checkGoal(tileMap);
        checkCollectibles(tileMap);
        checkHazards(tileMap);
    }

    @Override
    public void routeInput(Vector2D inputVector, boolean jumpPressed, boolean backspacePressed) {
        this.moveDirection = inputVector;
        this.jumpPressed = jumpPressed;
    }
}
