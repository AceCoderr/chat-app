# Use OpenJDK image
FROM openjdk:21-jdk-slim

# Set working directory
WORKDIR /app

# Copy jar (after mvn package)
COPY target/*.jar app.jar

# Expose port
EXPOSE 8080

# Start app
ENTRYPOINT ["java", "-jar", "app.jar"]

