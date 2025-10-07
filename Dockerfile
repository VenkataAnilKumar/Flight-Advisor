## Multi-stage Dockerfile: build with Maven, run with a slim JDK
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml ./
COPY src/ src/
COPY data/ data/
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -DskipTests package

FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar
EXPOSE 8090
ENTRYPOINT ["java","-jar","/app/app.jar"]
