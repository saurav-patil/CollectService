FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src
COPY lib ./lib

RUN mvn install:install-file \
    -Dfile=lib/enc-decryption.jar \
    -DgroupId=com.vtransact \
    -DartifactId=enc-decryption \
    -Dversion=0.0.1 \
    -Dpackaging=jar

RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /app/target/CollectService-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]