package ru.disdev.model;

import java.util.HashMap;
import java.util.Map;

public class StateActionMap {

    private final Map<Integer, Action> map = new HashMap<>();

    public StateActionMap next(Action action) {
        map.put(map.size(), action);
        return this;
    }

    public Action get(int state) {
        return map.get(state);
    }
}
