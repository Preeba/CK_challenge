package com.css.challenge.service;

import com.css.challenge.client.Order;

import java.util.List;
import java.util.Optional;

interface Storage {
    boolean addOrder(Order order);
    boolean removeOrderById(String id);
    Optional<Order> getOrderById(String id);
    List<Order> getAllOrders();
    boolean isFull();

}
