FROM openjdk:21-jdk-slim
ARG JAR_FILE=target/vetivet-backend-1.0.0.jar
COPY ${JAR_FILE} app_vet.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app_vet.jar"]

