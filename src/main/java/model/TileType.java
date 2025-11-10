package model;

// Enum denoting the different types of tiles in the game
public enum TileType {
    GROUND, HAZARD, GOAL, COLLECTIBLE;

    public static TileType stringToTileType(String type) {
        return switch (type) {
            case "ground" -> TileType.GROUND;
            case "hazard" -> TileType.HAZARD;
            case "goal" -> TileType.GOAL;
            case "collectible" -> TileType.COLLECTIBLE;
            default -> null;
        };
    }
}
