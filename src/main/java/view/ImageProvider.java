package view;

import java.awt.image.BufferedImage;

// Interface that allows animations and singular sprites to be stored together
public interface ImageProvider {
    BufferedImage getActiveImage();
}
