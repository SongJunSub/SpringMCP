# Use a base image with Java 17
FROM eclipse-temurin:17-jdk-jammy as builder

# Set the working directory inside the container
WORKDIR /app

# Copy the Gradle wrapper and build files
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Copy the source code
COPY src src

# Make the Gradle wrapper executable
RUN chmod +x gradlew

# Build the application
RUN ./gradlew bootJar

# Use a smaller base image for the final application
FROM eclipse-temurin:17-jre-jammy

# Set the working directory
WORKDIR /app

# Copy the built JAR file from the builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose the port the application runs on
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
