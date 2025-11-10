package view;

import javax.swing.*;

// Simple game window that contains the GamePanel
public class GameWindow extends JFrame {

    private static final String GAME_TITLE = "Java 2D Platformer";

    private final GamePanel gamePanel = new GamePanel();

    public GameWindow() {
        super(GAME_TITLE);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  // X button on the top right of the window is enabled here
        this.setResizable(false);
        this.add(gamePanel);  // Adds the GamePanel as a component of the window
        this.pack();  // Resizes window to fit its components, or the GamePanel in this case
        this.setLocationRelativeTo(null);  // Centers window
        this.setLayout(null);
        this.setVisible(true);
    }

    public GamePanel getGamePanel() {
        return gamePanel;
    }
}
