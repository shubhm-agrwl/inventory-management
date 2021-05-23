#!/bin/bash

JAVA_EXECUTABLE="`which java`"

DIST_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )/../" && pwd )"
LIB_DIR="$DIST_DIR/lib"
CONF_DIR="$DIST_DIR/conf"

CLASSPATH=$CONF_DIR

for f in ${LIB_DIR}/*.jar; do
    CLASSPATH=${CLASSPATH}:$f;
done

echo "Starting Application ${project.version}."

echo "$JAVA_EXECUTABLE -cp "$CLASSPATH" com.atlan.shubham.ingestor.IngestorService"

$JAVA_EXECUTABLE -cp "$CLASSPATH" com.atlan.shubham.ingestor.IngestorService server $DIST_DIR/conf/config.yml