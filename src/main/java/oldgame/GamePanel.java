package oldgame;

import java.awt.*;

import javax.swing.JPanel;

public class GamePanel extends JPanel implements Runnable {
    // Screen Settings
    final int originalTileSize = 16;  // 16x16 tile
    final int scale = 3;  // Multiply with everything to scale small image sizes

    public final int tileSize = originalTileSize * scale;  // 48x48 tile
    // 4:3 ratio
    public final int maxScreenCol = 16;  // Amount of tiles horizontally
    public final int maxScreenRow = 12;  // Amount of tiles vertically
    public final int screenWidth = tileSize * maxScreenCol;  // 768 pixels
    public final int screenHeight = tileSize * maxScreenRow;  // 576 pixels

    TileManager tileM = new TileManager(this);
    KeyHandler keyH = new KeyHandler();
    Thread gameThread;
    public Player player = new Player(this, keyH);

    // World Settings
    public final int maxWorldCol = 50;
    public final int maxWorldRow = 50;
    public final int worldWidth = tileSize * maxWorldCol;
    public final int worldHeight = tileSize * maxWorldRow;

    // FPS
    double FPS = 60;

    public GamePanel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.addKeyListener(keyH);  // The panel can recognize key inputs
        this.setFocusable(true);  // Lets the panel be focused on, allowing inputs to go through
    }

    public void startGameThread() {
        gameThread = new Thread(this); // Class gets passed into thread
        gameThread.start();
    }

    public void update() {
        player.update();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D)g;

        // Draw order matters here, tiles before player
        tileM.draw(g2d);
        player.draw(g2d);

        g2d.dispose();  // Once drawing is done, release the resources it uses
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
                update();

                // 2nd Draw: draw the screen with the updated info
                repaint();  // This actually calls paintComponent
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
}
