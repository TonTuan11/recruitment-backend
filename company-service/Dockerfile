FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY target/company-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 5003

ENTRYPOINT ["java","-jar","app.jar"]