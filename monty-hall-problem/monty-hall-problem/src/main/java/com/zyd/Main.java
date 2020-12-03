package com.zyd;

import com.zyd.strategy.ChangeChoiceStrategy;
import com.zyd.strategy.ChooseStrategy;

/**
 * Monty Hall Problem
 */
public class Main {

    public static void main(String[] args) {
        int count = 3000;
        int correct = 0;
        for (int i = 0; i < count; i++) {
            // we can also use in-line lambda to replace the implemented NotChangeChoiceStrategy
            // if (play(new NotChangeChoiceStrategy)) {
            if (play(doors -> {})) {
                correct++;
            }
        }
        System.out.printf("not change choice: %d / %d = %.2f%%%n", correct, count, correct * 1.0 / count);

        correct = 0;
        for (int i = 0; i < count; i++) {
            if (play(new ChangeChoiceStrategy())) {
                correct++;
            }
        }
        System.out.printf("change choice: %d / %d = %.2f%%%n", correct, count, correct * 1.0 / count);
    }

    private static boolean play(ChooseStrategy strategy) {
        Player player = new Player(strategy);
        Host host = new Host();
        Stage stage = new Stage();

        // Player make his first choice
        stage.doOperation(player::chooseDoor);
        // System.out.println(stage);

        // Host open a door
        stage.doOperation(host::openDoor);
        // System.out.println(stage);

        // Player make his second choice
        stage.doOperation(player::chooseAgain);
        // System.out.println(stage);

        return stage.isCorrect();
    }
}
