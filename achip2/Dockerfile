FROM amd64/openjdk:17-ea-jdk
ARG JAR_FILE=target/achip-0.0.1-SNAPSHOT.jar
WORKDIR /opt/app
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","app.jar"]