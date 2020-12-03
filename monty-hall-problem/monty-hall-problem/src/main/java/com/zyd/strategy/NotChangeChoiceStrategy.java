package com.zyd.strategy;

import com.zyd.Door;

import java.util.List;

public class NotChangeChoiceStrategy implements ChooseStrategy {

    @Override
    public void chooseSecondTime(List<Door> doors) {
        // do nothing
    }
}
