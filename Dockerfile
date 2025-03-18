FROM eclipse-temurin:21 AS build
LABEL app.github="https://github.com/anas-didi95/anas-didi95-superapp"
LABEL author.name="Anas Juwaidi Bin Mohd Jeffry"
LABEL author.email="anas.didi95@tutamail.com"

WORKDIR /app
COPY build.gradle.kts settings.gradle.kts gradlew ./
COPY gradle/wrapper ./gradle/wrapper
COPY src ./src

RUN ./gradlew build -x test

###

FROM eclipse-temurin:21.0.6_7-jre-alpine-3.21 AS runner

RUN addgroup -S -g 1000 runner && adduser -S -u 1000 runner -G runner
USER runner

WORKDIR /app
COPY --from=build /app/build/libs/*-fat.jar app.jar
RUN mkdir .h2

ENTRYPOINT ["java", "-jar", "app.jar"]
