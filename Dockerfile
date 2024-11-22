# Use an official OpenJDK 21 runtime as a base image
FROM openjdk:21-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Copy the JAR file built by Maven
COPY target/demo-0.0.1-SNAPSHOT.jar app.jar

# Expose the port your Spring Boot application listens on
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
