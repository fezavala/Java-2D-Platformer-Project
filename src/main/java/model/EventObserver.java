package model;

import util.InputListener;
import util.Vector2D;

import java.util.ArrayList;
import java.util.List;

public class EventObserver {
    private final List<EventListener> listeners = new ArrayList<>();

    public void addListener(EventListener listener) {
        listeners.add(listener);
    }

    public void removeListener(EventListener listener) {
        listeners.remove(listener);
    }

    protected void notifyListeners(GameEvent gameEvent) {
        for (EventListener listener : listeners) {
            listener.processEvent(gameEvent);
        }
    }
}
