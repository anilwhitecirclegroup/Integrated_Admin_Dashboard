
# 1. Use an official Java runtime
FROM eclipse-temurin:17-jdk-alpine

# 2. Set working directory
WORKDIR /app

# 3. Copy Maven wrapper and project files
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
COPY src ./src

# 4. Give execution permission
RUN chmod +x mvnw

# 5. Build the application
RUN ./mvnw clean package -DskipTests

# 6. Expose port (optional but good practice)
EXPOSE 8080

# 7. Run with dynamic PORT (IMPORTANT FOR RENDER)
CMD ["sh", "-c", "java -jar target/admindashboard-0.0.1-SNAPSHOT.jar --server.port=${PORT}"]