# Use an official OpenJDK runtime as a parent image
FROM openjdk:17-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the built jar file
COPY target/vehicle-0.0.1-SNAPSHOT.jar app.jar

# Expose the port (Render will set the PORT env variable)
EXPOSE 8080

# Run the jar file
ENTRYPOINT ["java","-jar","app.jar"] 