package com.css.challenge.service;

import com.css.challenge.client.Action;
import com.css.challenge.client.Order;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class KitchenManager {
    private final TemperatureStorage heater = new TemperatureStorage("Heater", "hot", 6);
    private final TemperatureStorage cooler = new TemperatureStorage("Cooler", "cold", 6);
    private final TemperatureStorage shelf = new TemperatureStorage("Shelf", "room", 12);

    private final List<Action> actionLog;

    public KitchenManager(List<Action> actionLog) {
        this.actionLog = actionLog;
    }

    public void placeOrder(Order order) {
        boolean placed = false;

        if (order.getTemp().equals("hot") && heater.addOrder(order)) {
            placed = true;
        } else if (order.getTemp().equals("cold") && cooler.addOrder(order)) {
            placed = true;
        } else if (shelf.addOrder(order)) {
            placed = true;
        } else {
            // we need to move a shelf hot/cold order
            List<Order> shelfOrders = shelf.getAllOrders();
            for (Order o : shelfOrders) {
                if (o.getTemp().equals("hot") && !heater.isFull() && heater.addOrder(o)) {
                    shelf.removeOrderById(o.getId());
                    actionLog.add(new Action(Instant.now(), o.getId(), Action.MOVE));

                    if (shelf.addOrder(order)) {
                        placed = true;
                        break;
                    }
                } else if (o.getTemp().equals("cold") && !cooler.isFull() && cooler.addOrder(o)) {
                    shelf.removeOrderById(o.getId());
                    actionLog.add(new Action(Instant.now(), o.getId(), Action.MOVE));

                    if (shelf.addOrder(order)) {
                        placed = true;
                        break;
                    }
                }
            }

            // Discard least fresh item if still no room
            if (!placed) {
                Optional<Order> toDiscard = shelf.getAllOrders().stream().min(Comparator.comparingInt(Order::getFreshness));
                toDiscard.ifPresent(o -> {
                    shelf.removeOrderById(o.getId());
                    actionLog.add(new Action(Instant.now(), o.getId(), Action.DISCARD));
                    shelf.addOrder(order);
                });
            }
        }
        actionLog.add(new Action(Instant.now(), order.getId(), Action.PLACE));
    }

    public void pickupOrder(String id) {
        boolean found = heater.removeOrderById(id) || cooler.removeOrderById(id) || shelf.removeOrderById(id);
        if (found) {
            actionLog.add(new Action(Instant.now(), id, Action.PICKUP));
        }
    }
}
