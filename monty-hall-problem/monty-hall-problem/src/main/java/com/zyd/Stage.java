package com.zyd;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Stage {

    private List<Door> doors;
    private final Random random = new Random();

    public Stage() {
        this.doors = randomInitDoors();
    }

    public void doOperation(StageOperation op) {
        op.doOperation(doors);
    }

    private List<Door> randomInitDoors() {
        int trueIndex = random.nextInt(3);
        doors = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            doors.add(new Door(i, i == trueIndex, false, false));
        }
        return doors;
    }

    public boolean isCorrect() {
        return this.doors.stream().filter(Door::isAnswer).anyMatch(Door::isChosen);
    }

    @Override
    public String toString() {
        return "Stage{" +
                "doors=" + doors +
                '}';
    }
}
