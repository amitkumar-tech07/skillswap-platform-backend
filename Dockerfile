# Java ka base image (java 21) --> (mera project Java 21 use kar rha hai,)
FROM eclipse-temurin:21-jdk-jammy

# Maintainer info
LABEL maintainer="Amit Kumar <amitkr9942@gmail.com>"

# Set working directory inside container
WORKDIR /app

# Copy the built JAR into container
COPY target/*.jar skillswap-backend.jar

# Container ka port (Spring Boot) --> (same as application.properties / .env)
EXPOSE 8080

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "skillswap-backend.jar"]
