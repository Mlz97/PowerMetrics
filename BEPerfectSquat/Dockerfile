FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
RUN useradd --system --uid 1001 pwr && mkdir -p /data/videos && chown pwr /data/videos
COPY --from=build /build/target/*.jar app.jar
USER pwr
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
