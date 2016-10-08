#!/bin/bash

if mvn clean package; then
  java -jar target/ChatServer-1.0-jar-with-dependencies.jar -l "servers_remote.conf" -n mihiraNectar -v &
fi
