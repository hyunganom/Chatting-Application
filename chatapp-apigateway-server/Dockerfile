FROM openjdk:11-jdk-slim
VOLUME /tmp
COPY target/chatapp-apigateway-server-1.0.jar apigatewayserver.jar
ENTRYPOINT ["java", "-jar", "apigatewayserver.jar"]