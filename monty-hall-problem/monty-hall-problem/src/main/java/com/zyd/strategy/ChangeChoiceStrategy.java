package com.zyd.strategy;

import com.zyd.Door;

import java.util.List;

public class ChangeChoiceStrategy implements ChooseStrategy {

    @Override
    public void chooseSecondTime(List<Door> doors) {
        doors.stream().filter(d -> !d.isOpened()).forEach(d -> d.setChosen(!d.isChosen()));
    }
}
