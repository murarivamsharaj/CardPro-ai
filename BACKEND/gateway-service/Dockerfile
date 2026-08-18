# Stage 1: Build the JAR with Maven inside the container
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run with lightweight Alpine JRE
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

ENV PORT=8765
EXPOSE 8765

ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT} -jar app.jar"]