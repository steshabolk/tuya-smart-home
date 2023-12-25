FROM eclipse-temurin:17-jdk-jammy as builder

WORKDIR /api
COPY build.gradle.kts settings.gradle.kts gradlew ./
COPY gradle/ gradle
RUN ./gradlew clean build || return 0
COPY ./src ./src
RUN ./gradlew clean build -x test

FROM eclipse-temurin:17-jre-jammy
WORKDIR /api
COPY --from=builder /api/build/libs/*.jar /api/*.jar
EXPOSE 8087
ENTRYPOINT ["java", "--add-opens=java.base/java.lang=ALL-UNNAMED", "--add-opens=java.management/sun.management=ALL-UNNAMED", "--add-opens=java.base/sun.net=ALL-UNNAMED", "-jar", "/api/*.jar" ]
