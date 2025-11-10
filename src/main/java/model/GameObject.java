package model;

import util.Vector2D;

// Abstract class that represents object position and size
public abstract class GameObject {
    protected Vector2D worldPosition = new Vector2D();
    protected int width, height;
    protected Vector2D hitboxOffset = new Vector2D();  // Moves the hit box position, useful to line up hit box with visuals

    public Vector2D getWorldPosition() {
        return worldPosition;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public RectangleBox getHitBox() {
        return new RectangleBox(new Vector2D(worldPosition.add(hitboxOffset)), width, height);
    }

}
