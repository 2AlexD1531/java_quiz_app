# Этап сборки
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
# УДАЛИТЬ ЭТУ СТРОКУ: RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# Этап запуска
FROM eclipse-temurin:21-jre-alpine
RUN apk update && \
    apk add --no-cache curl
WORKDIR /app
COPY --from=build /app/target/javaquiz-1.0.0.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]