FROM eclipse-temurin:21-jdk-alpine
ENV TZ=Asia/Ho_Chi_Minh
WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 15678
ENTRYPOINT ["java", "-Duser.timezone=Asia/Ho_Chi_Minh", "-jar", "app.jar"]