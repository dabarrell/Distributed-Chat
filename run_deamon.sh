#!/bin/bash

if mvn clean package; then
  java -jar -Dtinylog.writer=file -Dtinylog.writer.filename=log.txt -Dtinylog.level=trace target/ChatServer-1.0-jar-with-dependencies.jar -l "server.conf" -n mihiraNectar &
fi
