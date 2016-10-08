#!/bin/bash

if mvn clean package; then
  java -jar target/Client-1.0-jar-with-dependencies.jar -p 19999 -h 115.146.95.8 -i mihira2 -d
fi
