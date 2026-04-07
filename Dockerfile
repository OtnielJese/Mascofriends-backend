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
ENTRYPOINT ["sh", "-c", "java -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE -Dspring.datasource.url=jdbc:postgresql://$SUPABASE_HOST:$SUPABASE_PORT/$SUPABASE_DATABASE -Dspring.datasource.username=$SUPABASE_USERNAME -Dspring.datasource.password=$SUPABASE_PASSWORD -Dapp.jwt.secret=$JWT_SECRET -jar app_vet.jar"]

