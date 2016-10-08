#!/bin/bash

if mvn clean package; then
  java -jar target/ChatServer-1.0-jar-with-dependencies.jar -l "server.conf" -n test &
fi
