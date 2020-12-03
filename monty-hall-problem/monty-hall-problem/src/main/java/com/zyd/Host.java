package com.zyd;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Host {

    /**
     * Host will open the door which does not have the price.
     * @param doors doors in stage
     */
    public void openDoor(List<Door> doors) {
        doors = doors.stream().filter(this::canOpen).collect(Collectors.toList());
        Collections.shuffle(doors);
        doors.get(0).setOpened(true);
    }

    // if a door is not the answer nor the chosen one, then can be opened.
    private boolean canOpen(Door door) {
        return !door.isAnswer() && !door.isChosen();
    }
}
