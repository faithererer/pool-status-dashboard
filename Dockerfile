# Stage 1: Build Vue.js frontend
FROM node:18-alpine AS frontend-builder
WORKDIR /app/web
COPY web/package*.json ./
RUN npm install
COPY web/ ./
RUN npm run build

# Stage 2: Build Spring Boot backend
FROM maven:3.8.4-openjdk-17 AS backend-builder
WORKDIR /app
COPY pom.xml .
COPY src ./src/
RUN mvn clean package -DskipTests

# Stage 3: Create final application image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# Copy the backend JAR file
COPY --from=backend-builder /app/target/*.jar app.jar
# Copy the built frontend assets into Spring Boot's static resources directory
COPY --from=frontend-builder /app/web/dist /app/src/main/resources/static
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]