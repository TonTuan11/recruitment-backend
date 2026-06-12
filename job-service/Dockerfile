FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY target/job-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 5001

ENTRYPOINT ["java","-jar","app.jar"]