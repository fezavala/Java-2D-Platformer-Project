package control;

import model.GameManager;
import view.GamePanel;
import view.GameWindow;

import javax.swing.*;

// Class that runs the main game loop thread, updates physics and view
public class GameController implements Runnable {

    private final GameManager gameManager = new GameManager();
    private final InputHandler input = new InputHandler();
    private final GameWindow gameWindow = new GameWindow();

    private Thread gameThread;

    private static final double FPS = 60.0;

    private void startGame() {
        GamePanel gamePanel = gameWindow.getGamePanel();
        gamePanel.addKeyListener(input);
        gamePanel.setGameManager(gameManager);
        input.addListener(gameManager.getPlayer());
        input.addListener(gameManager.getMenuInputListener());
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        // DeltaTime setup
        double oneBillion = 1000000000;  // 1 second = 1 billion nanoseconds
        double drawInterval = oneBillion / FPS;  // Creates the ratio of when the program draws
        double deltaTime = 0;
        long lastTime = System.nanoTime();
        long currentTime;
        long timer = 0;  //
        int drawCount = 0;

        while(gameThread != null) {

            // DeltaTime calculation
            currentTime = System.nanoTime();
            deltaTime += (currentTime - lastTime) / drawInterval;
            timer += currentTime - lastTime;
            lastTime = currentTime;

            if (deltaTime >= 1) {
                // 1st Update: update info such as character positions
                gameManager.update();

                // 2nd Draw: draw the screen with the updated info
                GamePanel gamePanel = gameWindow.getGamePanel();
//                label.repaint();
                gamePanel.repaint();  // This actually calls paintComponent in the gamePanel


                // Update timers
                deltaTime--;
                drawCount++;
            }

            // Display fps in console every second
            if(timer > oneBillion) {
                System.out.println("FPS: " + drawCount);
                drawCount = 0;
                timer = 0;
            }

        }
    }


    public static void main (String[] args) {
        GameController gameController = new GameController();
        gameController.startGame();
    }
}
