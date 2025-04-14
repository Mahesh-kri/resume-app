# Build stage
FROM maven:latest AS build
WORKDIR /app
RUN mvn -version
COPY pom.xml .
RUN mvn compile -q
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM openjdk:17-slim
WORKDIR /app
COPY --from=build /app/target/resume.extractor-0.0.1-SNAPSHOT.jar app.jar

# Set environment variables
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s \
  CMD curl -f http://localhost:8080/ || exit 1

# Start the application (shell form)
ENTRYPOINT java -Djava.security.egd=file:/dev/./urandom $JAVA_OPTS -jar app.jar