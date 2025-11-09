package view;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

// Class that stores a Buffered image that can be accessed and rendered
public class Sprite implements ImageProvider {
    private BufferedImage sprite;

    public Sprite(String imgPath) {
        this.setImageFromFile(imgPath);
    }

    public Sprite(BufferedImage sprite) {
        this.sprite = sprite;
    }

    public void setImageFromFile(String imgPath) {
        try {
            InputStream imageStream = getClass().getResourceAsStream(imgPath);
            sprite = ImageIO.read(ImageIO.createImageInputStream(imageStream));
            imageStream.close();
        } catch (IOException e) {
            System.out.println("Error loading image:");
            e.printStackTrace();
        }
    }

    @Override
    public BufferedImage getActiveImage() {
        return sprite;
    }
}
