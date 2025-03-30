FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/key-value-cache-1.0.0.jar app.jar

# Expose the port the app runs on
EXPOSE 7171

# Command to run the application with optimized JVM settings
ENTRYPOINT ["java", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=50", "-Xms1536m", "-Xmx1536m", "-XX:+AlwaysPreTouch", "-XX:+DisableExplicitGC", "-jar", "app.jar"]