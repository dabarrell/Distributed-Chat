#!/bin/bash

if mvn clean package; then
  if [ "$3" = "root" ]; then
    echo "<<<<<<<<<<<<<<<<<<< Running Root Server >>>>>>>>>>>>>>>>>>>>>"
    nohup java -jar -Dtinylog.writer="$2" -Dtinylog.writer.filename=log.txt -Dtinylog.level=trace target/ChatServer-1.0-jar-with-dependencies.jar -l "servers_remote.conf" -n "$1" &
  else
    echo "<<<<<<<<<<<<<<<<<<< Running New Server >>>>>>>>>>>>>>>>>>>>>"
    nohup java -jar -Dtinylog.writer="$2" -Dtinylog.writer.filename=log.txt -Dtinylog.level=trace target/ChatServer-1.0-jar-with-dependencies.jar -l "servers_remote2.conf" -n "$1" -a &
  fi

fi
