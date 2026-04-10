# Build stage
FROM maven:3.9.8-eclipse-temurin-17-alpine AS builder

WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests clean package

# Runtime stage (minimal Java 17 image)
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app
COPY --from=builder /build/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-XX:+UseContainerSupport","-jar","/app/app.jar"]
