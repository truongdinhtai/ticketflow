# Multi-stage build for api-gateway.
# Build context = repository root (multi-module reactor). See docker-compose.yml.

# ---- Stage 1: build ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

COPY pom.xml ./
COPY common/pom.xml common/pom.xml
COPY api-gateway/pom.xml api-gateway/pom.xml
RUN mvn -B -pl common,api-gateway -am dependency:go-offline

COPY common/src common/src
COPY api-gateway/src api-gateway/src
RUN mvn -B -pl api-gateway -am -DskipTests package

# ---- Stage 2: runtime ----
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app
RUN groupadd --system spring && useradd --system --gid spring spring
USER spring:spring
COPY --from=build /build/api-gateway/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
