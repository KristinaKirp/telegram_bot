FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests


FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY --from=build /app/target/TelegramBot-0.0.1-SNAPSHOT.jar app.jar

CMD ["java", "-jar", "app.jar"]