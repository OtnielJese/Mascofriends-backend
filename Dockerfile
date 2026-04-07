# Stage 1: Build
FROM amazoncorretto:17 as builder
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM amazoncorretto:17
COPY --from=builder /build/target/vetivet-backend-1.0.0.jar app_vet.jar
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE -jar app_vet.jar"]

