FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests -Dmaven.test.skip=true

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/card-service-*.jar app.jar

ENV PORT=8082
EXPOSE 8082

ENTRYPOINT ["java", "-Dserver.port=${PORT}", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]