FROM jelastic/maven:3.9.6-zulujdk-17.0.10-almalinux-9 AS javabuild
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src
RUN mvn clean package -DskipTests

FROM openjdk:17-jdk
COPY --from=javabuild /target/*.jar app/service.jar
WORKDIR /app
ENTRYPOINT ["java","-jar","service.jar"]