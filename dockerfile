FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY target/store-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 15678

ENTRYPOINT [ "java", "-jar", "app.jar" ]