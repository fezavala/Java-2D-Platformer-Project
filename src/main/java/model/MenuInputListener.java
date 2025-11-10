package model;

import util.InputListener;
import util.Vector2D;

// Class that observes input during menu sections
public class MenuInputListener implements InputListener {

    private Vector2D inputVector = new Vector2D();
    private boolean spaceDown = false;
    private boolean backspaceDown = false;

    private boolean readSpace = false;
    private boolean readBackspace = false;

    private boolean lockSpace = false;
    private boolean lockBackspace = false;

    private boolean spacePressed = false;
    private boolean backspacePressed = false;

    public Vector2D getInputVector() {
        return inputVector;
    }

    public boolean isSpaceDown() {
        return spaceDown;
    }

    public boolean isBackspaceDown() {
        return backspaceDown;
    }

    public boolean isSpacePressed() {
        return spacePressed;
    }

    public boolean isBackspacePressed() {
        return backspacePressed;
    }

    // This method checks for if buttons are pressed, as in one full down and up button press
    public void updatePressedStates() {
        if (spacePressed) spacePressed = false;
        if (backspacePressed) backspacePressed = false;

        if (spaceDown && !readSpace) {
            readSpace = true;
        }
        if (readSpace && !spaceDown) {
            readSpace = false;
            spacePressed = !lockSpace;
            lockSpace = false;
        }
        if (backspaceDown && !readBackspace) {
            readBackspace = true;
        }
        if (readBackspace && !backspaceDown) {
            readBackspace = false;
            backspacePressed = !lockBackspace;
            lockBackspace = false;
        }
    }

    // Prevents inputs from instantly happening, used to prevent level beaten menu from instantly disappearing
    public void resetPressedStates() {
        lockSpace = spaceDown;
        lockBackspace = backspaceDown;
    }

    @Override
    public void routeInput(Vector2D inputVector, boolean jumpPressed, boolean backspacePressed) {
        this.inputVector = inputVector;
        this.spaceDown = jumpPressed;
        this.backspaceDown = backspacePressed;
    }
}
