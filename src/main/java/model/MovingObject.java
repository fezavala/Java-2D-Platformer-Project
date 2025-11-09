package model;

import util.Vector2D;

// NOTE: This class may be removed

public abstract class MovingObject extends GameObject {
    protected Vector2D velocity = new Vector2D();

//    MovingObject(float worldX, float worldY, int width, int height) {
//        this.worldX = worldX;
//        this.worldY = worldY;
//        this.width = width;
//        this.height = height;
//    }

    public abstract void move(TileMap tileMap);
}
