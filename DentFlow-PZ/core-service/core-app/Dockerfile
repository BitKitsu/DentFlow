# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Kopiuj wszystko do budowy
COPY . .

# Zbuduj JAR
RUN mvn -pl core-service/core-app -am package -DskipTests -q

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/core-service/core-app/target/*.jar app.jar

ENV SERVER_PORT=${PORT:-8080}
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java -jar app.jar"]
