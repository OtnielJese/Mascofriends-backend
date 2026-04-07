FROM amazoncorretto:17
ARG JAR_FILE=target/vetivet-backend-1.0.0.jar
COPY ${JAR_FILE} app_vet.jar
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE -jar app_vet.jar"]

