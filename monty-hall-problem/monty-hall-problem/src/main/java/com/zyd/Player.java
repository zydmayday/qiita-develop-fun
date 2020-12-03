package com.zyd;

import com.zyd.strategy.ChooseStrategy;

import java.util.List;

public class Player {

    private final ChooseStrategy strategy;

    public Player(ChooseStrategy strategy) {
        this.strategy = strategy;
    }

    /**
     * Player choose the door based on his strategy.
     * @param doors doors in stage
     */
    public void chooseDoor(List<Door> doors) {
        strategy.chooseFirstTime(doors);
    }

    /**
     * Player choose the door again based on his strategy.
     * @param doors doors in stage
     */
    public void chooseAgain(List<Door> doors) {
        strategy.chooseSecondTime(doors);
    }
}
