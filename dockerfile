FROM eclipse-temurin:21-jdk-alpine

ENV TZ=Asia/Ho_Chi_Minh

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 15678

ENTRYPOINT [ "java", "-jar", "app.jar" ]