#!/bin/bash

if mvn clean package; then
  nohup java -jar -Dtinylog.writer="$2" -Dtinylog.writer.filename=log.txt -Dtinylog.level=trace target/ChatServer-1.0-jar-with-dependencies.jar -l "servers_remote.conf" -n "$1" &
fi
