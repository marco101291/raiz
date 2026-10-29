# Build: dependencies first, in their own layer, so a code change does not download them again.
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN ./mvnw -B -q dependency:go-offline
COPY src src
# CI already ran the tests; the integration ones would need Docker inside the build.
RUN ./mvnw -B -q package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system raiz
COPY --from=build /app/target/raiz-backend-*.jar app.jar
USER raiz
# Fits the 512 MB of Render's free plan and starts faster on a fraction of a CPU.
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -Xss512k"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
