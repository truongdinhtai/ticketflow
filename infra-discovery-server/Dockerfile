# Multi-stage build for infra-discovery-server (Eureka).
# Build context = repository root (multi-module reactor). See docker-compose.yml.

# ---- Stage 1: build ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

COPY pom.xml ./
COPY common/pom.xml common/pom.xml
COPY infra-discovery-server/pom.xml infra-discovery-server/pom.xml
RUN mvn -B -pl common,infra-discovery-server -am dependency:go-offline

COPY common/src common/src
COPY infra-discovery-server/src infra-discovery-server/src
RUN mvn -B -pl infra-discovery-server -am -DskipTests package

# ---- Stage 2: runtime ----
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app
RUN groupadd --system spring && useradd --system --gid spring spring
USER spring:spring
COPY --from=build /build/infra-discovery-server/target/*.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
