FROM openjdk:11

ENV SERVICE_JAR=system-tests-helper-service-0.0.1.jar
ENV SERVICE_PATH=/usr/local/symantec/system-tests-helper-service
ENV PVC_PATH=/data/systemtests

RUN mkdir -p $SERVICE_PATH

COPY target/system-tests-helper-service-0.0.1.jar $SERVICE_PATH/
COPY docker/run.sh $SERVICE_PATH/
COPY docker/jacocoagent.jar $SERVICE_PATH/
COPY docker/jacococli.jar $SERVICE_PATH/

RUN chmod -R 755 ${SERVICE_PATH}

WORKDIR ${SERVICE_PATH}

CMD ["./run.sh"]
