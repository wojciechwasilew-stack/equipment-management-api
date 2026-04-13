FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml .
COPY domain/pom.xml domain/
COPY application/pom.xml application/
COPY infrastructure/pom.xml infrastructure/
COPY bootstrap/pom.xml bootstrap/

RUN mvn dependency:go-offline -B || true

COPY domain/src domain/src
COPY application/src application/src
COPY infrastructure/src infrastructure/src
COPY bootstrap/src bootstrap/src

RUN mvn clean package -DskipTests -B

FROM eclipse-temurin:21-jre

RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system appgroup && useradd --system --gid appgroup appuser

WORKDIR /app

COPY --from=build /workspace/bootstrap/target/*.jar app.jar

RUN chown -R appuser:appgroup /app

USER appuser

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
