# ==========================================
# Stage 1: Build & Package with Maven
# ==========================================
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /build

# Optimize layer caching: download dependencies first
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build production artifact
COPY src ./src
RUN mvn clean package -DskipTests -B

# ==========================================
# Stage 2: Hardened Production Runtime
# ==========================================
FROM eclipse-temurin:21-jre-alpine AS runtime

# Install curl for container healthchecks
RUN apk add --no-cache curl tzdata

# Security: Create non-root unprivileged service account
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Prepare writable directories for uploads and logs
RUN mkdir -p /app/uploads/resumes /app/logs && \
    chown -R appuser:appgroup /app

# Copy executable jar from builder stage
COPY --from=builder --chown=appuser:appgroup /build/target/*.jar app.jar

# JVM Performance Tuning for Containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"
ENV SPRING_PROFILES_ACTIVE="postgres"
ENV PORT=8080

EXPOSE 8080

# Switch to non-root user
USER appuser

# Healthcheck validating Spring Boot Actuator endpoint
HEALTHCHECK --interval=15s --timeout=5s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
