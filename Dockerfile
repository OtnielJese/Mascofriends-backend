# Stage 1: Build
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY . .
RUN apk add --no-cache maven && mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/vetivet-backend-1.0.0.jar app_vet.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app_vet.jar"]

