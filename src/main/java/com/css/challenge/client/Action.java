package com.css.challenge.client;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Action is a json-friendly representation of an action.
 */
public class Action {
    private final long timestamp; // unix timestamp in microseconds
    private final String id; // order id
    private final ActionType action; // place, move, pickup or discard

    public Action(Instant timestamp, String id, ActionType action) {
        this.timestamp = ChronoUnit.MICROS.between(Instant.EPOCH, timestamp);
        this.id = id;
        this.action = action;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getId() {
        return id;
    }

    public ActionType getAction() {
        return action;
    }

    @Override
    public String toString() {
        return "{timestamp: " + timestamp + ", id: " + id + ", action: " + action + " }";
    }
}
