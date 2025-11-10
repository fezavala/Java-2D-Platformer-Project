package view;

import util.Vector2D;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;

// Class that displays the HUD for the game
// Also displays the main menu
// Also displays the Level Complete Menu, a menu that appears once the player has beaten a level:
// The Level Complete Menu Displays:
// - Collectibles collected and its high score for the current level
// - Final level time and its high score for the current level
// - New record yellow text if applicable

// NOTE: I do not know if this is true or not, but using a thread for this game
// prevents JLabels from working properly, so instead this class is made
// which uses Graphics2D capabilities to mimic JLabel behavior. I could not get
// JLabels to work so this will suffice for the project.
public class GameHUD {

    // Universal static fields for size and font
    private static final int HUD_RENDER_UNIT = 32;
    private static final int HUD_SPRITE_MULTIPLIER = 2;
    private static final Font HUD_FONT = new Font("Arial", Font.PLAIN, HUD_RENDER_UNIT);
    private static final Stroke FONT_OUTLINE_SIZE = new BasicStroke(4);

    // Game HUD positioning static variables
    private static final Vector2D COLLECTIBLE_POSITION = new Vector2D(16, 16);
    private static final Vector2D COLLECTIBLE_TEXT_OFFSET = new Vector2D(COLLECTIBLE_POSITION.x + HUD_RENDER_UNIT + 8, COLLECTIBLE_POSITION.y + HUD_RENDER_UNIT + 20);
    private static final Vector2D TIMER_POSITION = new Vector2D(HUD_RENDER_UNIT * 24, 0);
//    private static final Color FONT_COLOR_OUTLINE = new Color(80, 47, 0);

    // Level complete menu text and positioning static variables
    private static final String NEW_RECORD_TEXT = "New Record!";
    private static final String NEXT_ACTION_TEXT_1 = "Backspace: Try Again";
    private static final String NEXT_ACTION_TEXT_2 = "Space: Next Level";
    private static final Color MENU_BACKGROUND_COLOR = new Color(0, 0, 0, 155);
    private static final Vector2D MENU_BACKGROUND_SIZE = new Vector2D(15 * HUD_RENDER_UNIT, 11 * HUD_RENDER_UNIT);
    private static final Vector2D MENU_BACKGROUND_POSITION = new Vector2D(7 * HUD_RENDER_UNIT, 2 * HUD_RENDER_UNIT);
    private static final Vector2D LEVEL_COMPLETE_TEXT_POSITION = new Vector2D(10 * HUD_RENDER_UNIT + 20, 4 * HUD_RENDER_UNIT);
    private static final Vector2D MENU_COLLECTIBLE_POSITION = new Vector2D(8 * HUD_RENDER_UNIT, 5 * HUD_RENDER_UNIT);
    private static final Vector2D MENU_COLLECTIBLE_TEXT_POSITION = new Vector2D(-HUD_RENDER_UNIT + 20, 2 * HUD_RENDER_UNIT + 12);
    private static final Vector2D FIRST_RECORD_TEXT_POSITION = new Vector2D(5 * HUD_RENDER_UNIT, 0);
    private static final Vector2D MENU_TIME_TEXT_POSITION = new Vector2D(-6 * HUD_RENDER_UNIT - 16, 2 * HUD_RENDER_UNIT);
    private static final Vector2D SECOND_RECORD_TEXT_POSITION = new Vector2D(6 * HUD_RENDER_UNIT + 16, 0);
    private static final Vector2D FIRST_ACTION_TEXT_POSITION = new Vector2D(-6 * HUD_RENDER_UNIT - 16, 2 * HUD_RENDER_UNIT);
    private static final Vector2D SECOND_ACTION_TEXT_POSITION = new Vector2D(0, 1.5 * HUD_RENDER_UNIT);

    // Main menu text and positioning static variables
    private static final String PLATFORMER_TEXT = "2D Platformer";
    private static final String PLAY_TEXT = "Press Space to play!";
    private static final String CONTROL_TEXT = "Controls:";
    private static final String MOVEMENT_TEXT = "AD or ←→: Move";
    private static final String JUMP_TEXT = "Space: Jump";
    private static final Vector2D JAVA_LOGO_SIZE = new Vector2D(120, 235);
    private static final Vector2D JAVA_LOGO_POSITION = new Vector2D(12 * HUD_RENDER_UNIT + 16, HUD_RENDER_UNIT);
    private static final Vector2D TITLE_TEXT_POSITION = new Vector2D(11 * HUD_RENDER_UNIT + 8, 9 * HUD_RENDER_UNIT + 16);
    private static final Vector2D PLAY_TEXT_POSITION = new Vector2D(-2 * HUD_RENDER_UNIT + 20, 3 * HUD_RENDER_UNIT);
    private static final Vector2D CONTROL_TEXT_POSITION = new Vector2D(3 * HUD_RENDER_UNIT, 6 * HUD_RENDER_UNIT);
    private static final Vector2D MOVEMENT_TEXT_POSITION = new Vector2D(-2 * HUD_RENDER_UNIT, 1.5 * HUD_RENDER_UNIT);
    private static final Vector2D JUMP_TEXT_POSITION = new Vector2D(HUD_RENDER_UNIT, 1.5 * HUD_RENDER_UNIT);
    private static final Vector2D[] MENU_COIN_POSITIONS = new Vector2D[] {
            new Vector2D(HUD_RENDER_UNIT, HUD_RENDER_UNIT),
            new Vector2D(26 * HUD_RENDER_UNIT, HUD_RENDER_UNIT),
            new Vector2D(HUD_RENDER_UNIT, 12 *HUD_RENDER_UNIT),
            new Vector2D(26 *HUD_RENDER_UNIT, 12 * HUD_RENDER_UNIT),
    };

    // Timer static variables
    private static final double MILLI_TO_SECOND = 1000;
    private static final double SECOND_TO_MINUTE = 60;

    // Sprites to render
    private final ImageProvider collectibleSprite;
    private final ImageProvider javaLogoSprite;


    public GameHUD(ImageProvider collectibleSprite, ImageProvider javaLogoSprite) {
        this.collectibleSprite = collectibleSprite;
        this.javaLogoSprite = javaLogoSprite;
    }

    // Turns a time (in milliseconds) double into a string of format "0:00" in seconds(left side) and minutes(right side)
    private String formatLevelTime(double time) {
        double seconds = Math.floor(time / MILLI_TO_SECOND);
        double minutes = Math.floor(seconds / SECOND_TO_MINUTE);
        double remainingSeconds = seconds - SECOND_TO_MINUTE * minutes;

        return String.format("%2.0f:%02.0f", minutes, remainingSeconds);
    }

    private Shape createTextShapeFromText(Graphics2D g2d, String text) {
        FontRenderContext frc = g2d.getFontRenderContext();
        TextLayout layout = new TextLayout(text, HUD_FONT, frc);
        return layout.getOutline(null);
    }

    private void drawTextShape(Graphics2D g2d, Shape textShape, Color borderColor, Vector2D position) {
        g2d.setColor(borderColor);
        g2d.translate(position.x, position.y);
        g2d.draw(textShape);
        g2d.setColor(Color.WHITE);
        g2d.fill(textShape);
    }

    private void resetDrawingPosition(Graphics2D g2d, Vector2D... positions) {
        for (Vector2D position : positions) {
            g2d.translate(-position.x, -position.y);
        }
    }

    // Menus and HUDs
    // NOTE: Shapes are created every frame, which I find is not efficient for rendering, but will do for this project.

    // Draws the in game hud, with a collectible counter on the top-left and a timer on the top-right
    public void drawGameHUD(Graphics2D g2d, int collectibleCount, double timerCount) {
        String collectibleText = String.valueOf(collectibleCount);
        String timerText = formatLevelTime(timerCount);

        // Anti-Alias the shapes
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Creating the text shapes for rendering
        Shape timerOutline = createTextShapeFromText(g2d, timerText);
        Shape coinCountOutline = createTextShapeFromText(g2d, collectibleText);

        // Drawing collectible count
        g2d.drawImage(collectibleSprite.getActiveImage(),
                (int)COLLECTIBLE_POSITION.x,
                (int)COLLECTIBLE_POSITION.y,
                HUD_RENDER_UNIT * HUD_SPRITE_MULTIPLIER,
                HUD_RENDER_UNIT * HUD_SPRITE_MULTIPLIER,
                null);

        // Preparing to render text
        g2d.setStroke(FONT_OUTLINE_SIZE);

        //Drawing coin text
        drawTextShape(g2d, coinCountOutline, Color.DARK_GRAY, COLLECTIBLE_TEXT_OFFSET);

        // Drawing timer text
        drawTextShape(g2d, timerOutline, Color.DARK_GRAY, TIMER_POSITION);

        // Resetting g2d position for cleanup
        resetDrawingPosition(g2d, COLLECTIBLE_TEXT_OFFSET, TIMER_POSITION);
    }

    // Draws the Level Complete menu
    public void drawLevelCompleteMenu(Graphics2D g2d, int currentLevel, int collectibleCount, double timerCount, int savedCollectibleCount, double savedTimerCount, boolean collectibleRecord, boolean timeRecord) {
        String collectibleText = String.valueOf(collectibleCount);
        String timerText = "Time: " + formatLevelTime(timerCount);
        String levelCompleteText = "Level " + currentLevel + " complete!";

        String collectibleRecordText = collectibleRecord ? NEW_RECORD_TEXT : "Best: " + savedCollectibleCount;
        String timeRecordText = timeRecord ? NEW_RECORD_TEXT : "Best: " + formatLevelTime(savedTimerCount);

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Creating text shapes for rendering
        Shape levelCompleteOutline = createTextShapeFromText(g2d, levelCompleteText);
        Shape timerOutline = createTextShapeFromText(g2d, timerText);
        Shape coinCountOutline = createTextShapeFromText(g2d, collectibleText);
        Shape firstRecordOutline = createTextShapeFromText(g2d, collectibleRecordText);
        Shape secondRecordOutline = createTextShapeFromText(g2d, timeRecordText);
        Shape firstActionOutline = createTextShapeFromText(g2d, NEXT_ACTION_TEXT_1);
        Shape secondActionOutline = createTextShapeFromText(g2d, NEXT_ACTION_TEXT_2);

        // Drawing menu background
        g2d.setColor(MENU_BACKGROUND_COLOR);
        g2d.fillRect((int)MENU_BACKGROUND_POSITION.x, (int)MENU_BACKGROUND_POSITION.y, (int)MENU_BACKGROUND_SIZE.x, (int)MENU_BACKGROUND_SIZE.y);

        // Drawing collectible sprite
        g2d.drawImage(collectibleSprite.getActiveImage(),
                (int)MENU_COLLECTIBLE_POSITION.x,
                (int)MENU_COLLECTIBLE_POSITION.y,
                HUD_RENDER_UNIT * HUD_SPRITE_MULTIPLIER,
                HUD_RENDER_UNIT * HUD_SPRITE_MULTIPLIER,
                null);

        // Setting up text rendering
        g2d.setStroke(FONT_OUTLINE_SIZE);

        // Drawing Level Complete text
        drawTextShape(g2d, levelCompleteOutline, Color.BLACK, LEVEL_COMPLETE_TEXT_POSITION);

        // Drawing current level collectibles text
        drawTextShape(g2d, coinCountOutline, Color.BLACK, MENU_COLLECTIBLE_TEXT_POSITION);

        // Drawing either collectible New Record text or the saved collectible amount
        drawTextShape(g2d, firstRecordOutline, Color.BLACK, FIRST_RECORD_TEXT_POSITION);

        // Drawing Final Level Time
        drawTextShape(g2d, timerOutline, Color.BLACK, MENU_TIME_TEXT_POSITION);

        // Drawing either level timer New Record text or the saved final level time
        drawTextShape(g2d, secondRecordOutline, Color.BLACK, SECOND_RECORD_TEXT_POSITION);

        // Drawing first action text
        drawTextShape(g2d, firstActionOutline, Color.BLACK, FIRST_ACTION_TEXT_POSITION);
        // Drawing second action text
        drawTextShape(g2d, secondActionOutline, Color.BLACK, SECOND_ACTION_TEXT_POSITION);

        // Resetting g2d position for cleanup
        resetDrawingPosition(g2d, LEVEL_COMPLETE_TEXT_POSITION,
                MENU_COLLECTIBLE_TEXT_POSITION,
                FIRST_RECORD_TEXT_POSITION,
                SECOND_RECORD_TEXT_POSITION,
                MENU_TIME_TEXT_POSITION,
                FIRST_ACTION_TEXT_POSITION,
                SECOND_ACTION_TEXT_POSITION);
    }

    public void drawMainMenu(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Shape titleText = createTextShapeFromText(g2d, PLATFORMER_TEXT);
        Shape playText = createTextShapeFromText(g2d, PLAY_TEXT);
        Shape controlText = createTextShapeFromText(g2d, CONTROL_TEXT);
        Shape movementText = createTextShapeFromText(g2d, MOVEMENT_TEXT);
        Shape jumpText = createTextShapeFromText(g2d, JUMP_TEXT);

        // Drawing java logo
        g2d.drawImage(javaLogoSprite.getActiveImage(),
                (int)JAVA_LOGO_POSITION.x,
                (int)JAVA_LOGO_POSITION.y,
                (int)JAVA_LOGO_SIZE.x,
                (int)JAVA_LOGO_SIZE.y,
                null);

        // Drawing 4 coin sprites:
        for (Vector2D coinPosition : MENU_COIN_POSITIONS) {
            g2d.drawImage(collectibleSprite.getActiveImage(),
                    (int)coinPosition.x,
                    (int)coinPosition.y,
                    HUD_RENDER_UNIT * HUD_SPRITE_MULTIPLIER,
                    HUD_RENDER_UNIT * HUD_SPRITE_MULTIPLIER,
                    null);
        }


        g2d.setStroke(FONT_OUTLINE_SIZE);

        // Drawing title text
        drawTextShape(g2d, titleText, Color.BLACK, TITLE_TEXT_POSITION);

        // Drawing space to play text
        drawTextShape(g2d, playText, Color.BLACK, PLAY_TEXT_POSITION);

        // Resetting g2d position
        resetDrawingPosition(g2d, TITLE_TEXT_POSITION, PLAY_TEXT_POSITION);

        // Drawing Controls
        drawTextShape(g2d, controlText, Color.BLACK, CONTROL_TEXT_POSITION);
        drawTextShape(g2d, movementText, Color.BLACK, MOVEMENT_TEXT_POSITION);
        drawTextShape(g2d, jumpText, Color.BLACK, JUMP_TEXT_POSITION);

        // Resetting g2d position for cleanup
        resetDrawingPosition(g2d, CONTROL_TEXT_POSITION, MOVEMENT_TEXT_POSITION, JUMP_TEXT_POSITION);
    }
}
