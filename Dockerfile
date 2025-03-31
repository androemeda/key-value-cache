FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/key-value-cache-1.0.0.jar app.jar

# Expose the port the app runs on
EXPOSE 7171

# Ultra-optimized JVM settings for t3.small (2 cores, 2GB RAM)
ENTRYPOINT ["java", \
  "-server", \
  "-XX:+UseG1GC", \
  "-XX:MaxGCPauseMillis=20", \
  "-XX:InitiatingHeapOccupancyPercent=35", \
  "-XX:+ExplicitGCInvokesConcurrent", \
  "-Xms1536m", \
  "-Xmx1536m", \
  "-XX:+AlwaysPreTouch", \
  "-XX:+DisableExplicitGC", \
  "-XX:+UseStringDeduplication", \
  "-XX:+UseNUMA", \
  "-XX:+UseCompressedOops", \
  "-XX:SurvivorRatio=8", \
  "-XX:MaxTenuringThreshold=1", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar" \
]