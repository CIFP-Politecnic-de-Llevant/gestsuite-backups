FROM maven:3-amazoncorretto-17 as develop-stage-backups

RUN yum install -y https://dev.mysql.com/get/mysql80-community-release-el7-7.noarch.rpm && \
    yum install -y --nogpgcheck mysql-community-client && \
    yum clean all

WORKDIR /app

COPY /config/ /resources/

COPY /api/gestsuite-backups .
RUN mvn clean package -f pom.xml
ENTRYPOINT ["mvn","spring-boot:run","-f","pom.xml"]

FROM maven:3-amazoncorretto-17 as build-stage-backups

RUN yum install -y https://dev.mysql.com/get/mysql80-community-release-el7-7.noarch.rpm && \
    yum install -y --nogpgcheck mysql-community-client && \
    yum clean all

WORKDIR /resources

COPY /api/gestsuite-backups .
RUN mvn clean package -f pom.xml

FROM maven:3-amazoncorretto-17 as production-stage-backups

RUN yum install -y https://dev.mysql.com/get/mysql80-community-release-el7-7.noarch.rpm && \
    yum install -y --nogpgcheck mysql-community-client && \
    yum clean all

COPY --from=build-stage-backups /resources/target/backups-0.0.1-SNAPSHOT.jar backups.jar
COPY /config/ /resources/
ENTRYPOINT ["java","-jar","/backups.jar"]