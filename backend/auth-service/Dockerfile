# Multi-stage build for auth-service.
# Build context = repository root (multi-module reactor). See docker-compose.yml.

# ---- Stage 1: build ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Copy the parent POM and ALL module POMs so Maven can read the reactor model.
COPY pom.xml ./
COPY common/pom.xml common/pom.xml
COPY infra-config-server/pom.xml infra-config-server/pom.xml
COPY infra-discovery-server/pom.xml infra-discovery-server/pom.xml
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY event-service/pom.xml event-service/pom.xml
COPY booking-service/pom.xml booking-service/pom.xml
COPY notification-service/pom.xml notification-service/pom.xml
COPY auth-service/pom.xml auth-service/pom.xml
RUN mvn -B -pl common,auth-service -am dependency:go-offline

COPY common/src common/src
COPY auth-service/src auth-service/src
RUN mvn -B -pl auth-service -am -DskipTests package

# ---- Stage 2: runtime ----
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app
RUN groupadd --system spring && useradd --system --gid spring spring
USER spring:spring
COPY --from=build /build/auth-service/target/*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
