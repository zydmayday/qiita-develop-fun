package com.zyd.strategy;

import com.zyd.Door;

import java.util.List;
import java.util.Random;

/**
 * A strategy determine how player choose the door
 */
@FunctionalInterface
public interface ChooseStrategy {

    Random random = new Random();

    default void chooseFirstTime(List<Door> doors) {
        doors.get(random.nextInt(doors.size())).setChosen(true);
    }

    void chooseSecondTime(List<Door> doors);
}
