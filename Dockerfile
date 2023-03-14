FROM openjdk:17-alpine
LABEL maintainer="petr.vymola@oracle.com"
VOLUME /tmp
EXPOSE 8080
ARG JAR_FILE=target/erpchallenge-1.0.jar
ADD ${JAR_FILE} erpchallenge.jar
ADD config/application.properties config/application.properties
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/erpchallenge.jar"]