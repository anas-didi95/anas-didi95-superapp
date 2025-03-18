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

FROM eclipse-temurin:21-alpine

RUN addgroup -S -g 1000 deploy
RUN adduser -S -u 1000 deploy -G deploy
USER deploy

WORKDIR /app
COPY --from=build /app/build/libs/*-fat.jar app.jar
RUN mkdir .h2

ENTRYPOINT [ "sh", "-c" ]
CMD ["java -jar app.jar"]
