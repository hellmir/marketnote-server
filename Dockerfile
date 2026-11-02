ARG BASE_IMAGE=public.ecr.aws/docker/library/eclipse-temurin:21-jre
FROM ${BASE_IMAGE}
RUN groupadd -r appuser && useradd -r -g appuser -d /home/appuser -s /sbin/nologin appuser
WORKDIR /app
EXPOSE 8080
ARG JAR_FILE
COPY ${JAR_FILE} /app/app.jar
USER appuser
ENTRYPOINT ["java","-jar","/app/app.jar"]
