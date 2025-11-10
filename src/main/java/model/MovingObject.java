package model;

import util.Vector2D;

// Abstract class for moving objects, only used by player, but can be later used to add enemies
public abstract class MovingObject extends GameObject {
    protected Vector2D velocity = new Vector2D();

    public abstract void move(TileMap tileMap);
}
