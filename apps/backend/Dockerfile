# apps/backend/Dockerfile

FROM gradle:8.10.2-jdk21 AS build
WORKDIR /app

COPY . .

RUN gradle :apps:backend:bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/apps/backend/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
