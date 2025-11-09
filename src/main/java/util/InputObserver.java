package util;

import java.util.ArrayList;
import java.util.List;

public abstract class InputObserver {
    private final List<InputListener> listeners = new ArrayList<>();

    public void addListener(InputListener listener) {
        listeners.add(listener);
    }

    public void removeListener(InputListener listener) {
        listeners.remove(listener);
    }

    protected void notifyListeners(Vector2D inputVector, boolean jumpPressed, boolean backspacePressed) {
        for (InputListener listener : listeners) {
            listener.routeInput(inputVector, jumpPressed, backspacePressed);
        }
    }
}
