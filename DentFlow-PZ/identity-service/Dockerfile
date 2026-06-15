# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Kopiuj wszystko do budowy
COPY . .

# Zbuduj JAR (pomiń testy, db nie jest dostepne w czasie budowy)
RUN mvn -pl identity-service -am package -DskipTests -q

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/identity-service/target/*.jar app.jar

ENV SERVER_PORT=${PORT:-8081}
EXPOSE 8081

ENTRYPOINT ["sh", "-c", "exec java -jar app.jar"]
