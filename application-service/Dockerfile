FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY target/application-service-0.0.1-SNAPSHOT.jar app.jar


EXPOSE 5002

ENTRYPOINT ["java","-jar","app.jar"]
