ARG BASE_IMAGE=public.ecr.aws/docker/library/eclipse-temurin:21-jre
FROM ${BASE_IMAGE}
RUN groupadd -r appuser && useradd -r -g appuser -d /home/appuser -s /sbin/nologin appuser
EXPOSE 8080
ARG JAR_FILE
COPY ${JAR_FILE} /app.jar
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1
USER appuser
ENTRYPOINT ["java","-jar","/app.jar"]
