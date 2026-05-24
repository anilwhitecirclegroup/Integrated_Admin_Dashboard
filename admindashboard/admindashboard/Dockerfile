# Use Java 17
FROM eclipse-temurin:17-jdk-alpine

# Set working directory
WORKDIR /app

# Copy from nested folder
COPY admindashboard/admindashboard/.mvn .mvn
COPY admindashboard/admindashboard/mvnw ./
COPY admindashboard/admindashboard/pom.xml ./
COPY admindashboard/admindashboard/src ./src

# Give permission
RUN chmod +x mvnw

# Build app
RUN ./mvnw clean package -DskipTests

# Expose port
EXPOSE 8080

# Run app
CMD ["sh", "-c", "java -jar target/admindashboard-0.0.1-SNAPSHOT.jar --server.port=${PORT}"]