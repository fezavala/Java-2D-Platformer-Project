package model;

public enum TileOrientation {
    TOP_LEFT, TOP, TOP_RIGHT, LEFT, CENTER, RIGHT, BOTTOM_LEFT, BOTTOM, BOTTOM_RIGHT;

    public static TileOrientation getOrientationFromInt(int orientation) {
        return switch (orientation) {
            case 0 -> TileOrientation.TOP_LEFT;
            case 1 -> TileOrientation.TOP;
            case 2 -> TileOrientation.TOP_RIGHT;
            case 3 -> TileOrientation.LEFT;
            case 4 -> TileOrientation.CENTER;
            case 5 -> TileOrientation.RIGHT;
            case 6 -> TileOrientation.BOTTOM_LEFT;
            case 7 -> TileOrientation.BOTTOM;
            case 8 -> TileOrientation.BOTTOM_RIGHT;
            default -> null;
        };
    }
}
