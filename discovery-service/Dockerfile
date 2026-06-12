#java 21 image
FROM eclipse-temurin:21-jdk
#working directory in container
WORKDIR /app
#copy java into the container
COPY target/discovery-service-0.0.1-SNAPSHOT.jar app.jar
#port service
EXPOSE 8762
#command to start a container
ENTRYPOINT ["java","-jar","app.jar"]