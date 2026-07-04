FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY target/user-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 5000

ENTRYPOINT ["java","-jar","app.jar"]


#
#FROM maven:3.9.9-eclipse-temurin-21 AS build
#
#WORKDIR /app
#
## Copy common-lib
#COPY common-lib ./common-lib
#
## Cài common-lib vào local Maven
#WORKDIR /app/common-lib
#RUN mvn clean install -DskipTests
#
## Build user-service
#WORKDIR /app/user-service
#COPY user-service/pom.xml .
#COPY user-service/src ./src
#
#RUN mvn clean package -DskipTests
#
## Runtime
#FROM eclipse-temurin:21-jdk
#
#WORKDIR /app
#
#COPY --from=build /app/user-service/target/user-service-0.0.1-SNAPSHOT.jar app.jar
#
#EXPOSE 5000
#
#ENTRYPOINT ["java","-jar","app.jar"]