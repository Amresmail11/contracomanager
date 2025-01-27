FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline

COPY src ./src

RUN ./mvnw package -DskipTests

EXPOSE 10000

ENV PORT=10000
CMD ["sh", "-c", "java -jar -Dspring.profiles.active=prod -Dserver.port=$PORT target/ContracoManager-0.0.1-SNAPSHOT.jar"] 