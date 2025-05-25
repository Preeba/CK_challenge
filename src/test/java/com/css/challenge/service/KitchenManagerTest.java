package com.css.challenge.service;

import com.css.challenge.client.Action;
import com.css.challenge.client.ActionType;
import com.css.challenge.client.Order;
import com.css.challenge.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.css.challenge.client.ActionType.PICKUP;
import static com.css.challenge.client.ActionType.PLACE;
import static com.css.challenge.util.TestUtils.createOrder;
import static org.junit.jupiter.api.Assertions.*;

class KitchenManagerTest {

    private List<Action> actions;
    private KitchenManager manager;

    @BeforeEach
    void setUp() {
        actions = new ArrayList<>();
        manager = new KitchenManager(actions);
    }

    @Test
    public void testLoadOrdersFromJson() throws IOException {
        List<Order> orders = TestUtils.loadProblemFromJson("/smallOrders.json");
        assertEquals(3, orders.size());

        Order first = orders.get(0);
        assertEquals("1", first.getId());
        assertEquals("Raspberries", first.getName());
        assertEquals("room", first.getTemp());
        assertEquals(97, first.getFreshness());
    }

    @Test
    public void testPlaceAndPickupOrderTiming() throws IOException {
        // Load sample data from JSON
        List<Order> orders = TestUtils.loadProblemFromJson("/largeOrders.json");
        assertEquals(48, orders.size());

        Order order = orders.get(0);
        manager.placeOrder(order);

        // simulate time between place and pickup
        try {
            Thread.sleep(4500); // > 4 seconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        manager.pickupOrder(order.getId());

        assertEquals(2, actions.size());
        assertEquals(PLACE, actions.get(0).getAction());
        assertEquals(PICKUP, actions.get(1).getAction());

        // Pickup timestamp should be greater than place timestamp
        long placeTime = actions.get(0).getTimestamp();
        long pickupTime = actions.get(1).getTimestamp();

        long delayMillis = pickupTime - placeTime;

        assertTrue(delayMillis >= 4000, "Pickup should be at least 4s after place");
    }

    @Test
    public void testPlaceOrderRecordsAction() {
        Order order = createOrder("o1", "Pizza", "hot", 300);
        manager.placeOrder(order);

        assertEquals(1, actions.size());
        Action action = actions.get(0);
        assertEquals(PLACE, action.getAction());
        assertEquals("o1", action.getId());
        assertTrue(action.getTimestamp() > 0);
    }

    @Test
    // MOVE action happens when shelf is full but the heater/cooler has space.
    public void testMoveOrderFromShelfToHeater() {
        // Fill shelf with 10 hot + 2 cold = 12 total
        for (int i = 0; i < 10; i++) {
            manager.placeOrder(createOrder("s" + i, "ShelfItem" + i, "hot", 100));
        }
        manager.placeOrder(createOrder("coldItem1", "ColdItem1", "cold", 100));
        manager.placeOrder(createOrder("coldItem2_to_move", "ColdItem2", "cold", 100));
        // At this point, heater has s0..s5, and shelf has s6..s9, cooler has coldItem1 & coldItem2_to_move

        // Fill the heater & cooler (capacity is 6)
        for (int i = 0; i < 6; i++) {
            manager.placeOrder(createOrder("h" + i, "HeaterItem" + i, "hot", 100));
            manager.placeOrder(createOrder("c" + i, "CoolerItem" + i, "cold", 100));
        }
        // At this point: Heater: Full (6/6), Cooler: Full (6/6) & Shelf: Full (12/12), make 1 space in cooler by picking order
        manager.pickupOrder("coldItem2_to_move");

        // Place a new hot order to trigger move. At this point, heater is full, shelf is full and cooler has 1 space, could move cold orders
        Order newOrder = createOrder("newHot", "soup", "hot", 100);
        manager.placeOrder(newOrder);

        boolean moveOccurred = actions.stream()
                .anyMatch(a -> a.getAction().equals(ActionType.MOVE));
        boolean placeOccurred = actions.stream()
                .anyMatch(a -> a.getId().equalsIgnoreCase("newHot") && a.getAction().equals(PLACE));

        assertTrue(moveOccurred, "Expected a MOVE action from shelf to heater");
        assertTrue(placeOccurred, "New order should be placed after MOVE");
    }


    @Test
    // DISCARD action happens when all shelves (including heater/cooler) are full and we need to make space.
    public void testOrderDiscardedToPlaceMoreOrder() {
        // fill heater & cooler to capacity (max is 6)
        for (int i = 0; i < 6; i++) {
            manager.placeOrder(createOrder("hot"+i, "hot item "+ i, "hot", 100));
            manager.placeOrder(createOrder("cold"+i, "cold item "+ i, "cold", 100));
        }

        // Now heater and cooler are full. Fill shelf to capacity with mix of hot/cold/room orders
        for (int i = 0; i < 12; i++) {
            String temp = (i % 2 == 0) ? "hot" : "cold";
            manager.placeOrder(createOrder("s" + i, "ShelfItem" + i, temp, 100 - i)); // freshness decreases
        }

        // Now shelf, heater, cooler are full to capacity

        // Now place a new hot order – this should trigger a DISCARD
        Order newOrder = createOrder("newHot", "OverflowItem", "hot", 100);
        manager.placeOrder(newOrder);

        // assert actions
        boolean discardOccurred = actions.stream().anyMatch(a -> a.getAction().equals(ActionType.DISCARD));
        boolean placeOccurred = actions.stream().anyMatch(a ->  a.getId().equalsIgnoreCase("newHot") && a.getAction().equals(PLACE));

        System.out.println("Actions:");
        actions.forEach(System.out::println);

        assertTrue(discardOccurred, "Expected a DISCARD action when all shelves are full");
        assertTrue(placeOccurred, "New order should still be placed after discard");

        Optional<String> discardedId = actions.stream()
                .filter(a -> a.getAction() == ActionType.DISCARD)
                .map(Action::getId)
                .findFirst();

        assertEquals("s11", discardedId.get());
    }

    // ----- pickupOrder

    @Test
    public void testPickupOrderWithoutPlaceOrderRecordsNoAction() {
        // Simulate placed order
        Order order = createOrder("o2", "Sushi", "cold", 300);
        manager.pickupOrder(order.getId());

        assertEquals(0, actions.size());
    }

    @Test
    public void testPickupFromHeaterIsSuccessfullyPickedUp() {
        Order hotOrder = createOrder("h1", "Hot Soup", "hot", 100);
        manager.placeOrder(hotOrder);
        manager.pickupOrder("h1");

        assertEquals(2, actions.size());
        assertEquals(PICKUP, actions.get(1).getAction());
        assertEquals("h1", actions.get(1).getId());
    }

    @Test
    public void testPickupFromCoolerIsSuccessfullyPickedUp() {
        Order coldOrder = createOrder("c1", "Ice Cream", "cold", 100);
        manager.placeOrder(coldOrder);
        manager.pickupOrder("c1");

        assertEquals(2, actions.size());
        assertEquals(PICKUP, actions.get(1).getAction());
        assertEquals("c1", actions.get(1).getId());
    }

    @Test
    public void testPickupFromShelf() {
        // Fill heater so next hot order goes to shelf (6/6 capacity)
        for (int i = 0; i < 6; i++) {
            manager.placeOrder(createOrder("h" + i, "HotItem" + i, "hot", 100));
        }

        Order shelfOrder = createOrder("shelf1", "OverflowHot", "hot", 100);
        manager.placeOrder(shelfOrder); // Goes to shelf due to full heater

        manager.pickupOrder("shelf1");

        assertEquals(2 + 6, actions.size()); // 6 heater orders + shelf + pickup
        assertEquals(PICKUP, actions.get(actions.size() - 1).getAction());
        assertEquals("shelf1", actions.get(actions.size() - 1).getId());
    }

    @Test
    public void testRepeatedPickupOnlyRecordsOnce() {
        Order order = createOrder("r1", "RoomTemp", "room", 100);
        manager.placeOrder(order);
        manager.pickupOrder("r1");
        manager.pickupOrder("r1"); // Should do nothing

        long pickupCount = actions.stream().filter(a -> a.getAction() == PICKUP).count();
        assertEquals(1, pickupCount);
    }

    @Test
    public void testPickupWithInvalidIdDoesNothing() {
        manager.pickupOrder("nonexistent-id");
        assertEquals(0, actions.size());
    }
}