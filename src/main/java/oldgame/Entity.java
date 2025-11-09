package oldgame;

import java.awt.image.BufferedImage;

public class Entity {
    public int worldX, worldY;  // Coordinates of entity
    public int speed;  // In pixels

    // BufferedImage class describes image with accessible buffer of image data
    public BufferedImage up1, up2, down1, down2, left1, left2, right1, right2;
    public String direction;

    public int spriteCounter = 0;
    public int spriteNum = 1;
}
