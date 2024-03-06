FROM eclipse-temurin:17
LABEL authors="franciscoroyo"

COPY target/ecommerce-backend-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]