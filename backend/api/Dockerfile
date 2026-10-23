# build stage: JDK image compiles the JAR (target/ is not in the repository)
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY src src
RUN chmod +x mvnw && ./mvnw -q -DskipTests package

# run stage: smaller JRE image
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
# Render supplies PORT; SPRING_DATASOURCE_URL is set in the Render dashboard
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
