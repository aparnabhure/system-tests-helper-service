#!/usr/bin/env bash

# Default variables passed via ENV if unset/blank
if [ -z "$JVM_HEAP_MIN" ]; then L_HEAPMIN="256m"; else L_HEAPMIN="$JVM_HEAP_MIN"; fi
if [ -z "$JVM_HEAP_MAX" ]; then L_HEAPMAX="1024m"; else L_HEAPMAX="$JVM_HEAP_MAX"; fi

# Runtime code coverage collection - if the jacocoagent.jar is present in the
# service directory, and one of the properties files contains
# "jacoco.code.coverage=true" , then collect coverage data.


mkdir -p $PVC_PATH/jars
cp $SERVICE_PATH/jacocoagent.jar $PVC_PATH/jars/jacocoagent.jar
cp $SERVICE_PATH/jacococli.jar $PVC_PATH/jars/jacococli.jar

# Spring profiles - read the configuration files outside of the service code
JAVA_ARGS_PROFILES=""
JAVA_ARGS_CONFIG="-Dspring.config.location=classpath:application.properties"

JAVA_ARGS_SERVER="-server -Dcatalina.base=$APP_HOME"
JAVA_ARGS_APP_HOME="-DAPP_HOME=$APP_HOME"
JAVA_ARGS_MEMORY="-Xms$L_HEAPMIN -Xmx$L_HEAPMAX"
JAVA_ARGS_TOUCH="-DTOUCH_FILE_PATH=$SERVICE_PATH/temp"

JAVA_ARGS="$JAVA_ARGS_SERVER $JAVA_ARGS_APP_HOME $JAVA_ARGS_MEMORY $JAVA_ARGS_PROFILES $JAVA_ARGS_CONFIG $JAVA_ARGS_TOUCH"

exec java $JAVA_ARGS -jar $SERVICE_JAR