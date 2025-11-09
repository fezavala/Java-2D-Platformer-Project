package view;

import javax.swing.*;

// Simple game window that contains the game panel
public class GameWindow extends JFrame {

    private final GamePanel gamePanel = new GamePanel();

    public GameWindow() {
        super("Java 2D Platformer");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  // X button is enabled here
        this.setResizable(false);
        this.add(gamePanel);
        this.pack();  // Resizes window to fit its components, or the GamePanel in this case
        this.setLocationRelativeTo(null);  // Centers window
        this.setLayout(null);
        this.setVisible(true);
    }

    public GamePanel getGamePanel() {
        return gamePanel;
    }
}
