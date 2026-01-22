# 1. Use an official Java runtime
FROM eclipse-temurin:17-jdk-alpine

# 2. Set working folder
WORKDIR /app

# 3. Copy only the necessary source files
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
COPY src ./src

# 4. Build the app (skipping tests to save time)
RUN ./mvnw clean package -DskipTests

# 5. Run the built jar file
# (Note: This finds any jar file in target folder)
CMD ["java", "-jar", "target/admindashboard-0.0.1-SNAPSHOT.jar"]
