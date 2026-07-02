##java 21 image
#FROM eclipse-temurin:21-jdk
##working directory in container
#WORKDIR /app
##copy java into the container
#COPY target/discovery-service-0.0.1-SNAPSHOT.jar app.jar
##port service
#EXPOSE 8762
##command to start a container
#ENTRYPOINT ["java","-jar","app.jar"]


FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY --from=build /app/target/discovery-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8762

ENTRYPOINT ["java","-jar","app.jar"]