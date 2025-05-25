package com.css.challenge;

import com.css.challenge.client.Action;
import com.css.challenge.client.Client;
import com.css.challenge.client.Order;
import com.css.challenge.client.Problem;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.css.challenge.service.KitchenManager;
import org.apache.log4j.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "challenge", showDefaultValues = true)
public class Main implements Runnable {
  private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

  static {
    org.apache.log4j.Logger.getRootLogger().setLevel(Level.OFF);
    System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tF %1$tT: %5$s %n");
  }

  @Option(names = "--endpoint", description = "Problem server endpoint")
  String endpoint = "https://api.cloudkitchens.com";

  @Option(names = "--auth", description = "Authentication token (required)")
  String auth = "";

  @Option(names = "--name", description = "Problem name. Leave blank (optional)")
  String name = "";

  @Option(names = "--seed", description = "Problem seed (random if zero)")
  long seed = 0;

  @Option(names = "--rate", description = "Inverse order rate")
  Duration rate = Duration.ofMillis(500);

  @Option(names = "--min", description = "Minimum pickup time")
  Duration min = Duration.ofSeconds(4);

  @Option(names = "--max", description = "Maximum pickup time")
  Duration max = Duration.ofSeconds(8);

  @Override
  public void run() {
    try {
      Client client = new Client(endpoint, auth);
      Problem problem = client.newProblem(name, seed);

      // ------ Simulation harness logic goes here using rate, min and max ----

      List<Action> actions = new ArrayList<>();
      KitchenManager manager = new KitchenManager(actions);
      ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

      List<Order> orders = problem.getOrders();
      for (int i = 0; i < orders.size(); i++) {
        Order order = orders.get(i);
        long delay = rate.toMillis() * i;

        // Schedule order placement
        scheduler.schedule(() -> {
          LOGGER.info("Received: {}", order);
          manager.placeOrder(order);

          // Schedule pickup after a random delay between min and max
          long pickupDelay = min.toMillis() + (long) (Math.random() * (max.toMillis() - min.toMillis()));
          scheduler.schedule(() -> {
            LOGGER.info("PickedUp: {}", order);
            manager.pickupOrder(order.getId());
          }, pickupDelay, TimeUnit.MILLISECONDS);

        }, delay, TimeUnit.MILLISECONDS);
      }

      // Shut down the scheduler after last order and pickup time
      long totalDuration = rate.toMillis() * orders.size() + max.toMillis() + 1000;
      scheduler.schedule(() -> {
        scheduler.shutdown();
        try {
          scheduler.awaitTermination(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
          LOGGER.error("Scheduler termination interrupted");
        }

        // ----------------------------------------------------------------------
        // Debugging to see actionsLog
        for (Action a : actions) {
          LOGGER.info("Action: {} | OrderID: {} | Time: {}", a.getAction(), a.getId(), a.getTimestamp());
        }
        // ----------------------------------------------------------------------
        try {
          String result = client.solveProblem(problem.getTestId(), rate, min, max, actions);
          LOGGER.info("Result: {}", result);

        } catch (IOException e) {
          LOGGER.error("Solve problem failed: {}", e.getMessage());
        }
      }, totalDuration, TimeUnit.MILLISECONDS);
    } catch (IOException e) {
      LOGGER.error("Simulation failed: {}", e.getMessage());
    }
  }

  public static void main(String[] args) {
    new CommandLine(new Main()).execute(args);
  }
}
