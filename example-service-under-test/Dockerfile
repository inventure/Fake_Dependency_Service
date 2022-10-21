# https://hub.docker.com/_/gradle. If updating this version, update Jenkinsfile as well.
ARG GRADLE_IMAGE=gradle:7.5.1-jdk11
FROM ${GRADLE_IMAGE} as clean_build
ARG GRADLE_TARGETS="clean assemble -Dorg.gradle.jvmargs=-Xmx8g -Dkotlin.daemon.jvm.options=-Xmx6g"
ARG JAR_FILE="example-service-under-test-0.0.1-SNAPSHOT.jar"
COPY --chown=1000:1000 build.gradle.kts settings.gradle.kts ./
COPY --chown=1000:1000 ./src ./src
RUN gradle ${GRADLE_ARGS} ${GRADLE_TARGETS} --info

FROM openjdk:11-jre-slim
# Declare contain user variables
ARG CONTAINER_USER_NAME=acceptance
ARG CONTAINER_USER_ID=10001

# Create application user
RUN id -u $CONTAINER_USER_NAME 2>/dev/null || useradd --system --create-home --uid $CONTAINER_USER_ID \
    --gid 0 $CONTAINER_USER_NAME

WORKDIR /app
RUN chown -R acceptance:root /app/
ADD src/main/resources /app/resources
COPY --from=clean_build --chown=acceptance:root /home/gradle/build/libs/${JAR_FILE} /app/${JAR_FILE}
COPY --from=clean_build --chown=acceptance:root /home/gradle/build/libs/${JAR_FILE} /home/gradle/${CONFIG_FILE} ./
EXPOSE 8080
# Set application user as default user
USER $CONTAINER_USER_NAME
ENTRYPOINT java -jar "example-service-under-test-0.0.1-SNAPSHOT.jar" $JAVA_OPTS
