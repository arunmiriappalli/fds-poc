# syntax=docker/dockerfile:1

# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src
COPY pom.xml .
COPY src ./src
# build and list artifacts so you can see the jar in the logs
RUN mvn -DskipTests package && ls -l target

# ---- Run stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app
# copy the fat jar produced by Spring Boot repackage
COPY --from=build /src/target/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
