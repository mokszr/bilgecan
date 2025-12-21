FROM eclipse-temurin:25
WORKDIR /opt/bilgecan
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} /opt/bilgecan/app.jar
ENTRYPOINT ["java","-jar","/opt/bilgecan/app.jar"]