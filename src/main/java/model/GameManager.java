package model;

import io.GameLoader;
import io.GameSaver;
import org.json.JSONObject;
import util.InputListener;
import util.Vector2D;

import java.util.HashMap;

// Manages and stores the games state, referenced and controlled by the Controller classes
public class GameManager implements EventListener {

    private static final int LEVEL_AMOUNT = 5;
    private static final int TILE_SIZE = 32;

    private final Player player;
    private TileMap tileMap;

    // Input listener for menu inputs
    private final MenuInputListener menuInputListener = new MenuInputListener();

    private GameState gameState = GameState.MAIN_MENU;

    // HashMap that stores the save data for the game, saved after each level is beat
    private final HashMap<Integer, HashMap<LevelData, Double>> levelSaveData = new HashMap<>(LEVEL_AMOUNT);

    // Level tracking variables
    private int currentLevel = 0;
    private int levelCollectedCollectibles = 0;
    private double previousTime = 0.0;
    private boolean startTimer = true;
    private double levelTimer = 0.0;  // In milliseconds
    private boolean levelCollectibleRecord = false;
    private boolean levelTimeRecord = false;

    // Level transition variables
    private static final double TRANSITION_TIME = 1;
    private static final double TRANSITION_RATE = 0.03;
    private double currentTransitionTime = 0.0;


    public GameManager() {
        Vector2D playerPosition = new Vector2D();
        this.player = new Player(this, playerPosition);
        loadLevelSaveData();
    }

    // Takes a JSONObject from the GameLoader to load level save data
    private void loadLevelSaveData() {
        JSONObject levelSaveData = GameLoader.loadSaveData();
        if (levelSaveData != null) {
            for (String key : levelSaveData.keySet()) {
                JSONObject levelData = levelSaveData.getJSONObject(key);
                HashMap<LevelData, Double> levelSaveDataMap = new HashMap<>();
                for (String key2 : levelData.keySet()) {
                    levelSaveDataMap.put(LevelData.getFromString(key2), levelData.getDouble(key2));
                }
                this.levelSaveData.put(Integer.valueOf(key), levelSaveDataMap);
            }
        } else {
            double defaultCollectibleCount = 0.0;
            for (int i = 1; i <= LEVEL_AMOUNT; i++) {
                HashMap<LevelData, Double> levelData = new HashMap<>();
                levelData.put(LevelData.COLLECTIBLES, defaultCollectibleCount);
                levelData.put(LevelData.TIME, Double.MAX_VALUE);
                this.levelSaveData.put(i, levelData);
            }
        }
    }

    private void saveLevelSaveData() {
        int savedCollectibleCount = getSavedLevelCollectibleCount();
        double savedLevelTime = getSavedLevelTime();
        if (savedLevelTime > levelTimer) {
            levelSaveData.get(currentLevel).put(LevelData.TIME, levelTimer);
            levelTimeRecord = true;
        }
        if (savedCollectibleCount < levelCollectedCollectibles) {
            levelSaveData.get(currentLevel).put(LevelData.COLLECTIBLES, (double)levelCollectedCollectibles);
            levelCollectibleRecord = true;
        }
        JSONObject levelSaveData = new JSONObject(this.levelSaveData);
        GameSaver.saveGame(levelSaveData);
    }

    private void changeLevel() {
        player.resetPlayer(new Vector2D());
        if (currentLevel == LEVEL_AMOUNT) {
            System.out.println("No More levels left :(");
            System.out.println("Back to the beginning");
            currentLevel = 0;
        }
        currentLevel++;
        loadLevel();
    }

    private void resetLevel() {
        player.resetPlayer(new Vector2D());
        levelCollectedCollectibles = 0;
        startTimer = true;
        tileMap.resetTileMap();
    }

    private void loadLevel() {
        levelCollectedCollectibles = 0;
        startTimer = true;
        String level = String.valueOf(currentLevel - 1);
        tileMap = new TileMap(level, TILE_SIZE);
    }

    private void updateLevelTimer() {
        double currentTime = System.currentTimeMillis();
        if (startTimer) {
            startTimer = false;
            levelTimer = 0.0;
            previousTime = currentTime;
            return;
        }
        levelTimer += (currentTime - previousTime);
        previousTime = currentTime;
    }

    // Main menu state
    private void menuState() {
        if (menuInputListener.isSpacePressed()) {
            gameState = GameState.LEVEL_TRANSITION;
            transitionState();
        }
    }

    // In game state
    private void inLevelState() {
        player.move(tileMap);
        updateLevelTimer();
    }

    // Transition state for animation
    private void transitionState() {
        currentTransitionTime += TRANSITION_RATE;
        if (currentTransitionTime >= TRANSITION_TIME) {
            changeLevel();
            gameState = GameState.IN_LEVEL;
            currentTransitionTime = 0.0;
        }
    }

    // level finish state that awaits input to change or reset level
    private void levelFinishedState() {
        if (menuInputListener.isBackspacePressed()) {
            // Reset the level to try again
            gameState = GameState.IN_LEVEL;
            levelCollectibleRecord = false;
            levelTimeRecord = false;
            resetLevel();
            return;
        }
        if (menuInputListener.isSpacePressed()) {
            gameState = GameState.LEVEL_TRANSITION;
            levelCollectibleRecord = false;
            levelTimeRecord = false;
            transitionState();
        }
    }

    // Main update loop for the game, called by the game controller every frame
    public void update() {
        // Update to make sure a press is recorded (button down to button up)
        menuInputListener.updatePressedStates();

        // Mini state machine that calls different methods depending
        // on the games current state.
        switch (gameState) {
            case MAIN_MENU -> menuState();
            case IN_LEVEL -> inLevelState();
            case LEVEL_TRANSITION -> transitionState();
            case LEVEL_FINISHED -> levelFinishedState();
        }
    }

    public int getTileSize() {
        return TILE_SIZE;
    }

    public InputListener getPlayer() {
        return player;
    }

    public Vector2D getPlayerWorldPosition() {
        return player.getWorldPosition();
    }

    // Public getters for the game state, used mainly by the GamePanel

    public PlayerState getPlayerState() { return player.getCurrentPlayerState(); }

    public double getPlayerHorizontalVelocity() { return player.getPlayerVelocity().x; }

    public Vector2D getPlayerCenterPosition() {
        return player.getPlayerCenterPosition();
    }

    public TileMap.Tile[] getVisibleTiles(Vector2D worldPos, Vector2D screenSize) {
        return tileMap.getVisibleTiles(worldPos, screenSize);
    }

    public int getCollectibleAmount() {
        return levelCollectedCollectibles;
    }

    public double getLevelTime() {
        return levelTimer;
    }

    public GameState getGameState() {
        return gameState;
    }

    public InputListener getMenuInputListener() {
        return menuInputListener;
    }

    public int getSavedLevelCollectibleCount() {
        return levelSaveData.get(currentLevel).get(LevelData.COLLECTIBLES).intValue();
    }

    public double getSavedLevelTime() {
        return levelSaveData.get(currentLevel).get(LevelData.TIME);
    }

    public boolean isLevelCollectibleRecord() {
        return levelCollectibleRecord;
    }

    public boolean isLevelTimeRecord() {
        return levelTimeRecord;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public boolean getTileMapActive() {
        return tileMap != null;
    }

    @Override
    public void processEvent(GameEvent gameEvent) {
        // Put event processing here
        switch (gameEvent) {
            case GOAL_REACHED -> {
                gameState = GameState.LEVEL_FINISHED;
                menuInputListener.resetPressedStates();
                saveLevelSaveData();
            }
            case DAMAGE -> {
                System.out.println("You have died, resetting level");
                resetLevel();
            }
            case COLLECTIBLE -> levelCollectedCollectibles++;
        }
    }

    // Enums for the level save data hash map
    private enum LevelData {
        COLLECTIBLES, TIME;

        public static LevelData getFromString(String str) {
            return switch (str) {
                case "COLLECTIBLES" -> COLLECTIBLES;
                case "TIME" -> TIME;
                default -> null;
            };
        }
    }
}
