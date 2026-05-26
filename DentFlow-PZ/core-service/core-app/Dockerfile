# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Kopiuj parent POM
COPY pom.xml ./

# Kopiuj wszystkie moduły core-service
COPY core-service/pom.xml ./core-service/
COPY core-service/catalog/pom.xml ./core-service/catalog/
COPY core-service/catalog/src ./core-service/catalog/src
COPY core-service/clinic/pom.xml ./core-service/clinic/
COPY core-service/clinic/src ./core-service/clinic/src
COPY core-service/notification/pom.xml ./core-service/notification/
COPY core-service/notification/src ./core-service/notification/src
COPY core-service/patient/pom.xml ./core-service/patient/
COPY core-service/patient/src ./core-service/patient/src
COPY core-service/reservation/pom.xml ./core-service/reservation/
COPY core-service/reservation/src ./core-service/reservation/src
COPY core-service/scheduling/pom.xml ./core-service/scheduling/
COPY core-service/scheduling/src ./core-service/scheduling/src
COPY core-service/core-app/pom.xml ./core-service/core-app/
COPY core-service/core-app/src ./core-service/core-app/src

# Pobierz zależności (cache layer)
RUN mvn -pl core-service/core-app -am dependency:go-offline -q

# Zbuduj JAR
RUN mvn -pl core-service/core-app -am package -DskipTests -q

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/core-service/core-app/target/*.jar app.jar

ENV SERVER_PORT=${PORT:-8080}
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java -jar app.jar"]
