# README

Author: `<your name here>`

## How to run

The `Dockerfile` defines a self-contained Java/Gradle reference environment.
Build and run the program using [Docker](https://docs.docker.com/get-started/get-docker/):
```
$ docker build -t challenge .
$ docker run --rm -it challenge --args="--auth=<token>"
```
Feel free to modify the `Dockerfile` as you see fit.

If java `21` or later is installed locally, run the program directly for convenience:
```
$ ./gradlew run --args="--auth=<token>"
```

# Example:
```bash
  $ ./gradlew run --args="--auth=kujbxc7wibq9
```

## Discard criteria

An order is discarded when heater(for hot orders), cooler (for cold orders) and overflow shelves are full and no space is available to place a new order or can't move orders.
In other words, an order is discarded when there is no space in target shelves (hot, cold or overflow) to place an order or move an order.

Example: 
1. Heater/cooler is full: 
   a. If an order is hot but hot shelf is full (or)
   b. order is cold but cold shelf is full
2. Overflow shelf is full or can't accept orders
3. Move attempt fails: If overflow shelf has space, but can't move an existing order to correct shelf to make room

### Rationale

The application mimics real-world scenario where shelf space capacity is a constant and orders are prioritized based on freshness.  

