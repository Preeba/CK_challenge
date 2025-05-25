package com.css.challenge;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

public class MainTest {

    @Test
    public void testRunWithSeedJson() {
        Main main = new Main();
        main.name = "Test Problem";
        main.rate = Duration.ofMillis(500);
        main.min = Duration.ofSeconds(4);
        main.max = Duration.ofSeconds(8);

        // Load seed from file
        Path seedPath = Paths.get("src/test/resources/seed.txt");
        main.seed = 3252744674990800375L;
        main.auth="kujbxc7wibq9";
        main.name="";

        // Run it
        main.run();

        // You can also assert things like actions, logs, etc. if you refactor to make them accessible
    }

}
