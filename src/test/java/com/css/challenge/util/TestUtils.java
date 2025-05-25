package com.css.challenge.util;

import com.css.challenge.client.Order;
import com.css.challenge.client.Problem;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class TestUtils {

    public static List<Order> loadProblemFromJson(String sourcePath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        InputStream is = TestUtils.class.getResourceAsStream(sourcePath);

        if (is == null) {
            throw new IllegalArgumentException("Test JSON file not found: " + sourcePath);
        }
        return mapper.readValue(is, new TypeReference<List<Order>>(){});
    }

    public static Order createOrder(String id, String name, String temp, int freshness) {
        return new Order(id, name, temp, freshness);
    }

}
