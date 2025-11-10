package model;

import io.MapLoader;
import org.json.JSONArray;
import org.json.JSONObject;
import util.Vector2D;

import java.util.HashMap;
import java.util.Stack;

// Class that stores a map of tiles and provides methods to obtain tiles efficiently whenever needed
public class TileMap {

    // This array stores offsets for finding surrounding tiles from the player much easier
    private static final Vector2D[] ENTITY_SURROUNDING_TILES = new Vector2D[]{
            new Vector2D(-1, -2), new Vector2D(0, -2), new Vector2D(1, -2),
            new Vector2D(-1, -1), new Vector2D(0, -1), new Vector2D(1, -1),
            new Vector2D(-1, 0), new Vector2D(0, 0), new Vector2D(1, 0),
            new Vector2D(-1, 1), new Vector2D(0, 1), new Vector2D(1, 1),
            new Vector2D(-1, 2), new Vector2D(0, 2), new Vector2D(1, 2)
    };

    // Spikes have a smaller hit box that needs to be accounted for manually
    private static final Vector2D STANDARD_TILE_OFFSET = new Vector2D(0, 0);
    private static final Vector2D BOTTOM_SPIKE_OFFSET = new Vector2D(0, 15);
    private static final Vector2D RIGHT_SPIKE_OFFSET = new Vector2D(15, 0);
    private static final int SPIKE_HITBOX_SIZE = 18;

    // Smaller hitbox for collectibles as well
    private static final int COLLECTIBLE_HITBOX_SIZE = 20;
    private static final Vector2D COLLECTIBLE_OFFSET = new Vector2D(7, 7);

    private final int tileSize;
    private final HashMap<Vector2D, Tile> interactableTiles;  // Tiles that the player interacts with
    private final Stack<Vector2D> currentlyDisabledTiles = new Stack<>();  // If a tile is disabled, it's position is store here to re-enable the tile later

    public TileMap(String mapName, int tileSize) {
        this.tileSize = tileSize;
        this.interactableTiles =  loadTileMap(mapName);
    }

    // Loads a map from the given mapName from a JSONObject
    private HashMap<Vector2D, Tile> loadTileMap(String mapName) {
        // Grab JSON object from MapLoader
        JSONObject mapObject = MapLoader.loadMapJSON(mapName);
        if (mapObject == null) return null;
        HashMap<Vector2D, Tile> tileMap = new HashMap<>(mapObject.length());
        for (String key : mapObject.keySet()) {
            JSONObject tileInfo = mapObject.getJSONObject(key);
            JSONArray posInfo = tileInfo.getJSONArray("pos");
            Vector2D pos = new Vector2D(posInfo.getDouble(0), posInfo.getDouble(1));
            Vector2D tileWorldPos = new Vector2D(pos.x * tileSize, pos.y * tileSize);
            TileType tileType = TileType.stringToTileType(tileInfo.getString("type"));
            TileOrientation tileOrientation = TileOrientation.getOrientationFromInt(tileInfo.getInt("variant"));

            Tile mapTile = generateMapTile(tileType, tileOrientation, tileWorldPos);
            tileMap.put(pos, mapTile);
        }
        return tileMap;
    }

    // Creates a tile based on its type, orientation, and position
    private Tile generateMapTile(TileType tileType, TileOrientation tileOrientation, Vector2D tileWorldPos) {
        Vector2D tileOffset = STANDARD_TILE_OFFSET;
        int tileWidth = tileSize;
        int tileHeight = tileSize;
        if (tileType == TileType.HAZARD) {
            switch (tileOrientation) {
                case TOP_LEFT -> {
                    tileOffset = BOTTOM_SPIKE_OFFSET;
                    tileHeight = SPIKE_HITBOX_SIZE;
                }
                case TOP -> tileWidth = SPIKE_HITBOX_SIZE;
                case TOP_RIGHT -> tileHeight = SPIKE_HITBOX_SIZE;
                case LEFT -> {
                    tileOffset = RIGHT_SPIKE_OFFSET;
                    tileWidth = SPIKE_HITBOX_SIZE;
                }
                case null, default -> {}
            }
        }
        if (tileType == TileType.COLLECTIBLE) {
            tileOffset = COLLECTIBLE_OFFSET;
            tileWidth = COLLECTIBLE_HITBOX_SIZE;
            tileHeight = COLLECTIBLE_HITBOX_SIZE;
        }

        return new Tile(tileWorldPos, tileOffset, tileWidth, tileHeight, tileType, tileOrientation, false);
    }

    // Grabs the hit boxes of tiles of the selected TileType surrounding the given worldPos
    public RectangleBox[] getSurroundingTileHitBoxes(Vector2D worldPos, TileType tileType) {
        RectangleBox[] hitBoxes = new RectangleBox[ENTITY_SURROUNDING_TILES.length];
        int tileX = (int) Math.floor(worldPos.x / tileSize);
        int tileY = (int) Math.floor(worldPos.y / tileSize);
        Vector2D centerPosition = new Vector2D(tileX, tileY);
        for (int i = 0; i < ENTITY_SURROUNDING_TILES.length; i++) {
            Vector2D tileOffset = ENTITY_SURROUNDING_TILES[i];
            Vector2D tilePosition = centerPosition.add(tileOffset);
            Tile tile = interactableTiles.get(tilePosition);
            if (tile == null) continue;
            if (tile.isDisabled()) continue;
            if (tile.type == tileType) hitBoxes[i] = tile.getHitBox();
        }
        return hitBoxes;
    }

    // Returns the tiles only visible within the screenBounds based on the worldPos provided. More efficient than
    // rendering entire TileMap for larger maps.
    // Note: worldPos must be the position at the top left of screen
    public Tile[] getVisibleTiles(Vector2D worldPos, Vector2D screenBounds) {
        int xTileAmount = (int)(screenBounds.x / tileSize) + 1; // +1 for an extra layer of tiles to be rendered just in case
        int yTileAmount = (int)(screenBounds.y / tileSize) + 1;

        // Flooring the result prevents negative values from being off by one once divided by tileSize
        int tileXOffset = (int) Math.floor(worldPos.x / tileSize);
        int tileYOffset = (int) Math.floor(worldPos.y / tileSize);

        Vector2D tilePositionOffset = new Vector2D(tileXOffset, tileYOffset);

        Tile[] visibleTiles = new Tile[xTileAmount * yTileAmount];
        int tileIndex = 0;
        for (int x = 0; x < xTileAmount; x++) {
            for (int y = 0; y < yTileAmount; y++) {
                Vector2D tilePos = new Vector2D(x + tilePositionOffset.x, y + tilePositionOffset.y);
                Tile tile = interactableTiles.get(tilePos);
                if (tile == null) continue;  // No tile in bound
                if (tile.isDisabled()) continue;  // Disabled tiles are not visible
                visibleTiles[tileIndex++] = tile;
            }
        }
        return visibleTiles;
    }

    // Turns off tile, preventing it from being visible or be collided with
    public void disableTile(Vector2D tileWorldPos, TileType tileType) {
        int tileX = (int) Math.floor(tileWorldPos.x / tileSize);
        int tileY = (int) Math.floor(tileWorldPos.y / tileSize);
        Vector2D tilePosition = new Vector2D(tileX, tileY);
        Tile foundTile = interactableTiles.get(tilePosition);

        if (foundTile == null) return;
        if (foundTile.type == tileType) {
            foundTile.setDisabled(true);
            currentlyDisabledTiles.push(tilePosition);
        }
    }

    // Used to reset map collectibles, better than reloading the map again
    public void resetTileMap() {
        while (!currentlyDisabledTiles.isEmpty()) {
            Vector2D tilePos = currentlyDisabledTiles.pop();
            Tile disabledTile = interactableTiles.get(tilePos);
            if (disabledTile == null) continue;
            disabledTile.setDisabled(false);
        }
    }

    // Interior tile class container that stores tile position and type, extends GameObject for hit box and hit box offset functionality
    public class Tile extends GameObject {

        private final TileType type;
        private final TileOrientation orientation;
        private boolean disabled;

        public Tile(Vector2D worldPosition, Vector2D hitboxOffset, int width, int height, TileType type, TileOrientation orientation, boolean disabled) {
            this.worldPosition = worldPosition;
            this.hitboxOffset = hitboxOffset;
            this.width = width;
            this.height = height;
            this.type = type;
            this.orientation = orientation;
            this.disabled = disabled;
        }

        public TileType getType() {
            return type;
        }
        public TileOrientation getOrientation() { return orientation; }
        public boolean isDisabled() { return disabled; }
        public void setDisabled (boolean disabled) { this.disabled = disabled; }
    }
}
