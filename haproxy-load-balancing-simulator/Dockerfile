#
# Author: canetizen
# Created on Sat May 31 2025
# Description: Dockerfile
#

FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN apk add --no-cache maven && mvn clean package -DskipTests
EXPOSE 8080
CMD ["java", "-jar", "target/haproxy-load-balancing-simulator-0.0.1-SNAPSHOT.jar"]
