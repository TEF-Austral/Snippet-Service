FROM gradle:8.5.0-jdk21-alpine AS build

# Accept build arguments for GitHub credentials
ARG GITHUB_ACTOR
ARG REDIS_TOKEN

ENV GITHUB_ACTOR=${GITHUB_ACTOR}
ENV REDIS_TOKEN=${REDIS_TOKEN}

COPY . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle :api:bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine
EXPOSE 8080
RUN mkdir /app
COPY --from=build /home/gradle/src/api/build/libs/*.jar /app/spring-boot-application.jar
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=production", "/app/spring-boot-application.jar"]