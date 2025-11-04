
FROM maven:3.9.4-eclipse-temurin-21 AS build
WORKDIR /app
COPY mvnw pom.xml ./
COPY .mvn .mvn
COPY src ./src
RUN chmod +x mvnw && ./mvnw clean package -DskipTests


FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app


RUN apk add --no-cache tzdata
ENV TZ="Asia/Kolkata"

COPY --from=build /app/target/jharkhand_project-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
