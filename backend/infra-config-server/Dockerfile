# =====================================================================
# Multi-stage build for infra-config-server.
# IMPORTANT: build context is the REPOSITORY ROOT (see docker-compose.yml),
# because this module is part of a multi-module Maven reactor and needs the
# parent POM + the shared "common" module to build.
#   docker build -f infra-config-server/Dockerfile -t ticketflow/config-server .
# =====================================================================

# ---- Stage 1: build the jar ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Copy only the POMs first so Maven can resolve dependencies in a cached layer
# Copy the parent POM and ALL module POMs. The parent aggregator lists every
# module, so Maven must be able to read them all to build the reactor model,
# even though we only build one module below.
COPY pom.xml ./
COPY common/pom.xml common/pom.xml
COPY infra-config-server/pom.xml infra-config-server/pom.xml
COPY infra-discovery-server/pom.xml infra-discovery-server/pom.xml
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY event-service/pom.xml event-service/pom.xml
COPY booking-service/pom.xml booking-service/pom.xml
COPY notification-service/pom.xml notification-service/pom.xml
COPY auth-service/pom.xml auth-service/pom.xml
RUN mvn -B -pl common,infra-config-server -am dependency:go-offline

# Now copy sources and build just this module (+ its required modules)
COPY common/src common/src
COPY infra-config-server/src infra-config-server/src
RUN mvn -B -pl infra-config-server -am -DskipTests package

# ---- Stage 2: slim runtime image ----
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

# Run as a non-root user for good measure
RUN groupadd --system spring && useradd --system --gid spring spring
USER spring:spring

COPY --from=build /build/infra-config-server/target/*.jar app.jar

EXPOSE 8888
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
