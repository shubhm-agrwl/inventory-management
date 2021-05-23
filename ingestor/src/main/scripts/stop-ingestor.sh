#!/bin/bash

PIDS=$(ps ax | grep -i 'IngestorService' | grep java | grep -v grep | awk '{print $1}')

if [ -z "$PIDS" ]; then
  echo "No ingestor service to stop"
  exit 1
else 
  kill -s TERM $PIDS
  echo "Stopped Ingestor Service"
fi
