package com.css.challenge.client;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ActionType {
    PLACE, MOVE, PICKUP, DISCARD;

    @JsonValue
    public String toLower() {
        return name().toLowerCase();
    }
}
