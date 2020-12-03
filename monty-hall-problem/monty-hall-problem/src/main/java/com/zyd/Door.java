package com.zyd;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Define a door
 */
@AllArgsConstructor
@Data
public class Door {

    int index;
    // is or not has the price
    boolean isAnswer;
    // is or not opened by host
    boolean isOpened;
    // is or not chosen by player
    boolean isChosen;

}
