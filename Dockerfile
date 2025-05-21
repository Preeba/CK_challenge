# syntax=docker/dockerfile:1
FROM gradle:8.4-jdk21
WORKDIR /app

COPY settings.gradle build.gradle ./
COPY src ./src

ENTRYPOINT ["/usr/bin/gradle", "run"]
