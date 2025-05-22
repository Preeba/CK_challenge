package com.css.challenge.service;

import com.css.challenge.client.Order;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class ShelfStorage implements Storage {

    private final String name;
    private final int capacity;
    private final String temperature;
    private final Map<String, Order> orders = new ConcurrentHashMap<>();

    public ShelfStorage(String name, String temperature, int capacity) {
        this.name = name;
        this.temperature = temperature;
        this.capacity = capacity;
    }

    @Override
    public boolean addOrder(Order order) {
        if (orders.size() < capacity) {
            orders.put(order.getId(), order);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeOrderById(String id) {
        return orders.remove(id)!= null;
    }

    @Override
    public Optional<Order> getOrderById(String id) {
        return Optional.ofNullable(orders.get(id));
    }

    @Override
    public List<Order> getAllOrders() {
        return new ArrayList<>(orders.values());
    }

    @Override
    public boolean isFull() {
        return orders.size() >= capacity;
    }
}
