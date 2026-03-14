# ----- ETAPA 1: Construcción de la aplicación -----

FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
LABEL authors="KEVIN"

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# ----- ETAPA 2: runtime -----

FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

ENTRYPOINT ["java","-jar","/app/app.jar"]