package control;

import util.InputObserver;
import util.Vector2D;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

// Class that handles key inputs and sends them to the appropriate model or view classes

public class InputHandler extends InputObserver implements KeyListener {

    private boolean upPressed, downPressed, leftPressed, rightPressed;
    private boolean jumpInput = false;
    private boolean backspaceInput = false;

    @Override
    public void keyTyped(KeyEvent e) {
        // Not used, just here to not throw an exception
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();  // Returns int keyCode associated with the key in this event

        if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP) {
            upPressed = true;
        }
        if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) {
            downPressed = true;
        }
        if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT) {
            leftPressed = true;
        }
        if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) {
            rightPressed = true;
        }
        if (code == KeyEvent.VK_SPACE) {
            jumpInput = true;
        }
        if (code == KeyEvent.VK_BACK_SPACE) {
            backspaceInput = true;
        }
        notifyListeners(getMovementVector(), jumpInput, backspaceInput);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();

        if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP) {
            upPressed = false;
        }
        if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) {
            downPressed = false;
        }
        if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT) {
            leftPressed = false;
        }
        if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) {
            rightPressed = false;
        }
        if (code == KeyEvent.VK_SPACE) {
            jumpInput = false;
        }
        if (code == KeyEvent.VK_BACK_SPACE) {
            backspaceInput = false;
        }
        notifyListeners(getMovementVector(), jumpInput, backspaceInput);
    }

    public Vector2D getMovementVector() {
        Vector2D inputVector = new Vector2D();
        if (upPressed) {
            inputVector.y--;
        }
        if (downPressed) {
            inputVector.y++;
        }
        if (leftPressed) {
            inputVector.x--;
        }
        if (rightPressed) {
            inputVector.x++;
        }
        return inputVector;
    }

    public boolean getJumpInput() {
        return jumpInput;
    }
}
