package com.example.ohmypc.api.turtle;

public interface ITurtleAPI {
    boolean forward();
    boolean back();
    boolean up();
    boolean down();
    boolean turnLeft();
    boolean turnRight();
    boolean dig();
    boolean digUp();
    boolean digDown();
    boolean place();
    boolean placeUp();
    boolean placeDown();
    int getFuelLevel();
    String inspect();
    int getSelectedSlot();
    void setSelectedSlot(int slot);
}