package com.zyd;

import java.util.List;

@FunctionalInterface
public interface StageOperation {

    void doOperation(List<Door> doors);
}
