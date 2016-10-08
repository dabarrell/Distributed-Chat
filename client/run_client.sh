#!/bin/bash

if mvn clean package; then
  java -jar `dirname $0`/target/Client-1.0-jar-with-dependencies.jar -p 19999 -h 115.146.95.8 -i $1 -d
fi
