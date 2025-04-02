FROM maven:3-amazoncorretto-17 as develop-stage-backups
WORKDIR /app

COPY /config/ /resources/

COPY /api/gestsuite-backups .
RUN mvn clean package -f pom.xml
ENTRYPOINT ["mvn","spring-boot:run","-f","pom.xml"]

FROM maven:3-amazoncorretto-17 as build-stage-backups
WORKDIR /resources

COPY /api/gestsuite-backups .
RUN mvn clean package -f pom.xml

FROM amazoncorretto:17-alpine-jdk as production-stage-backups
COPY --from=build-stage-backups /resources/target/backups-0.0.1-SNAPSHOT.jar backups.jar
COPY /config/ /resources/
ENTRYPOINT ["java","-jar","/backups.jar"]