# ---------- build stage ----------
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app

# Reduce rebuilds by copying pom first and downloading dependencies
COPY pom.xml .
RUN mvn -B -DskipTests dependency:go-offline

# Copy source and build
COPY src ./src
RUN mvn -B -DskipTests package

# ---------- runtime stage ----------
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy the fat jar from build stage (uses wildcard so artifactId/version changes are OK)
COPY --from=build /app/target/*.jar app.jar

# Render sets PORT (default 10000). We'll read it from env at startup.
ENV PORT=10000
EXPOSE 10000

# Start the jar and bind to the PORT env variable
ENTRYPOINT ["sh", "-c", "java -jar /app/app.jar --server.port=${PORT}"]
