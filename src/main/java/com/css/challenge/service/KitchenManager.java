package com.css.challenge.service;

import com.css.challenge.client.Action;
import com.css.challenge.client.ActionType;
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

        // Step1: Place order in hot/cold shelves based on the temperature and capacity
        if (tryHotOrColdShelf(order)) {
            logAction(order.getId(), ActionType.PLACE);
            return;
        }
        // Step2: Place in overflow shelf if hot/cold shelves are at capacity & overflow shelf is not full
        if (!shelf.isFull()) {
            if (shelf.addOrder(order)) {
                logAction(order.getId(), ActionType.PLACE);
                return;
            }
        }

        // Step3:Move a hot/cold order from shelf to make space
        List<Order> shelfOrders = shelf.getAllOrders();
        for (Order existingOrder : shelfOrders) {
            System.out.println("Trying to move: " + existingOrder.getId() + " (" + existingOrder.getTemp() + ")");

            // Check heater/cooler capacity and based on that try to move hot order or cold order
            boolean moved = false;
            if (isHot(existingOrder) && !heater.isFull()) {
                moved = moveAndPlaceOrder(existingOrder, heater, order);
            } else if (isCold(existingOrder) && !cooler.isFull()) {
                moved = moveAndPlaceOrder(existingOrder, cooler, order);
            }

            // If move successful, try placing new order in shelf
            if (moved) return;
        }

        // Step4: Discard least fresh order from shelf
        Optional<Order> toDiscard = shelf.getAllOrders().stream()
                .min(Comparator.comparingInt(Order::getFreshness));

        toDiscard.ifPresent(o -> {
            shelf.removeOrderById(o.getId());
            logAction(o.getId(), ActionType.DISCARD);
            shelf.addOrder(order);
            logAction(order.getId(), ActionType.PLACE);
        });
    }

    private boolean moveAndPlaceOrder(Order toMoveOrder, Storage target, Order newOrder) {
        shelf.removeOrderById(toMoveOrder.getId());

        if (!target.addOrder(toMoveOrder)) {
            return false;
        }

        logAction(toMoveOrder.getId(), ActionType.MOVE);
        if (shelf.addOrder(newOrder)) {
            logAction(newOrder.getId(), ActionType.PLACE);
        }
        return true;
    }

    private boolean tryHotOrColdShelf(Order order) {
        if (isHot(order) && !heater.isFull()) {
            return heater.addOrder(order);
        }
        if (isCold(order) && !cooler.isFull()) {
            return cooler.addOrder(order);
        }
        return false;
    }

    private static boolean isHot(Order order) {
        return order.getTemp().equalsIgnoreCase("hot");
    }

    private static boolean isCold(Order existingOrder) {
        return existingOrder.getTemp().equalsIgnoreCase("cold");
    }

    public void pickupOrder(String id) {
        boolean found = heater.removeOrderById(id) || cooler.removeOrderById(id) || shelf.removeOrderById(id);
        if (found) {
            logAction(id, ActionType.PICKUP);
        }
    }

    private void logAction(String orderId, ActionType actionType) {
        actionLog.add(new Action(Instant.now(), orderId, actionType));
    }

    /*
        Helper method used for debugging the tests
     */
    public void printStats() {
        System.out.println("PRINTING STATS.");
        System.out.println("Cooler size: " + cooler.getAllOrders().size());
        System.out.println("Cooler isFull: " + cooler.isFull());
        System.out.println("Heater size: " + heater.getAllOrders().size());
        System.out.println("Heater isFull: " + heater.isFull());
        System.out.println("shelf size: " + shelf.getAllOrders().size());
        System.out.println("Shelf isFull: " + shelf.isFull());
    }
}
