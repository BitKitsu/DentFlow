# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Kopiuj parent POM i moduły potrzebne do budowy
COPY pom.xml ./
COPY identity-service/pom.xml ./identity-service/
COPY identity-service/src ./identity-service/src

# Pobierz zależności (cache layer)
RUN mvn -pl identity-service -am dependency:go-offline -q

# Zbuduj JAR (pomiń testy - są uruchamiane w CI)
RUN mvn -pl identity-service -am package -DskipTests -q

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/identity-service/target/*.jar app.jar

ENV SERVER_PORT=${PORT:-8081}
EXPOSE 8081

ENTRYPOINT ["sh", "-c", "exec java -jar app.jar"]
