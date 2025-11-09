package view;

import java.awt.image.BufferedImage;

// Class that stores a sequence of sprites to be cycled through
public class SpriteAnimation implements ImageProvider {
    private final Sprite[] sprites;
    private final int spriteAmount;
    private final int frameSpeed;
    private int spriteIndex = 0;
    private int currentFrame = 0;

    public SpriteAnimation(Sprite[] sprites, int animSpeed) {
        this.sprites = sprites;
        this.spriteAmount = sprites.length;
        this.frameSpeed = animSpeed;
    }

    public void updateAnimation() {
        currentFrame++;
        if (currentFrame > frameSpeed) {
            spriteIndex = (spriteIndex + 1) % spriteAmount;
            currentFrame = 0;
        }
    }

    public void resetAnimation() {
        currentFrame = 0;
        spriteIndex = 0;
    }

    public BufferedImage getActiveImage() {
        return sprites[spriteIndex].getActiveImage();
    }
}
