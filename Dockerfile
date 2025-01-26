FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline

COPY src ./src

RUN ./mvnw package -DskipTests

EXPOSE 9090

CMD ["java", "-Dspring.profiles.active=prod", "-jar", "target/ContracoManager-0.0.1-SNAPSHOT.jar"] 