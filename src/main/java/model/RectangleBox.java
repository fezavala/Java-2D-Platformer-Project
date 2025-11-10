package model;

import util.Vector2D;

// AABB class
// This class is never updated live, it is simply instantiated on the spot for use wherever needed.
public class RectangleBox {
    private final Vector2D worldPosition;
    private final double width, height;

    public RectangleBox(Vector2D worldPosition, float width, float height) {
        this.worldPosition = worldPosition;
        this.width = width;
        this.height = height;
    }

    // Getters are used to determine where the collision happened
    public Vector2D getWorldPosition() {
        return worldPosition;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public double getTopSide() {
        return worldPosition.y;
    }

    public double getLeftSide() {
        return worldPosition.x;
    }

    public double getBottom () {
        return worldPosition.y + height;
    }

    public double getRightSide() {
        return worldPosition.x + width;
    }

    public Vector2D getCenter() {
        return new Vector2D(worldPosition.x + width / 2, worldPosition.y + height / 2);
    }


    // Returns true if this box and the input box intersects
    public boolean intersects(RectangleBox box) {
        return worldPosition.x < box.worldPosition.x + box.width &&
                worldPosition.x + width > box.worldPosition.x &&
                worldPosition.y < box.worldPosition.y + box.height &&
                worldPosition.y + height > box.worldPosition.y;
    }
}
