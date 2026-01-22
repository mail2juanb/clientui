# Use a lightweight Alpine image with JRE 21 (no need for JDK in production) or eclipse-temurin:21-jdk-jammy (quite heavy but with a JDK)
FROM eclipse-temurin:21-jre-alpine

# Sets the working directory in the container
WORKDIR /app

# Environment variables for configuration
ENV EUREKA_SERVER_HOST=eureka-server
ENV EUREKA_SERVER_PORT=9102

# Install curl (required for healthcheck)
RUN apk add --no-cache curl

# Copy the application jar file
COPY target/clientui-*.jar app.jar

# Expose the port listening
EXPOSE 8090

# Command to launch the application
ENTRYPOINT ["java", "-jar", "app.jar"]