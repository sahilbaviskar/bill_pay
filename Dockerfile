# Use Eclipse Temurin OpenJDK 17 as base image (more reliable)
FROM eclipse-temurin:17-jdk-alpine

# Install Maven, CA certificates, and networking tools for SSL connections
RUN apk add --no-cache maven ca-certificates openssl curl

# Update CA certificates
RUN update-ca-certificates

# Set working directory
WORKDIR /app

# Copy pom.xml first for better caching
COPY pom.xml .

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Expose port 8080
EXPOSE 8080

# Add JVM options for better networking and SSL handling
ENV JAVA_OPTS="-Djava.security.egd=file:/dev/./urandom -Djavax.net.ssl.trustStore=/opt/java/openjdk/lib/security/cacerts -Djavax.net.ssl.trustStorePassword=changeit"

# Run the application with JVM options
CMD ["sh", "-c", "java $JAVA_OPTS -jar target/split-app-backend-0.0.1-SNAPSHOT.jar"]