package model;

// Interface for classes that listen to game events
public interface EventListener {
    void processEvent(GameEvent gameEvent);
}
