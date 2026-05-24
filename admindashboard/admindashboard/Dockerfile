# 1. Use Java 17
FROM eclipse-temurin:17-jdk-alpine

# 2. Set working directory
WORKDIR /app

# 3. Copy project files
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
COPY src ./src

# 4. Give permission
RUN chmod +x mvnw

# 5. Build application
RUN ./mvnw clean package -DskipTests

# 6. Expose port
EXPOSE 8080

# 7. Run application (Render compatible)
CMD ["sh", "-c", "java -jar target/admindashboard-0.0.1-SNAPSHOT.jar --server.port=${PORT}"]