#!/bin/bash

if mvn clean package; then
  java -jar target/AdminAddUser-1.0-jar-with-dependencies.jar -l "../servers_remote.conf" -i $1 -v
fi
