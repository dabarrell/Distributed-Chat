#!/bin/bash

# First arg must be user name
# Second arg must be password

if mvn clean package; then
  java -jar `dirname $0`/target/Client-1.0-jar-with-dependencies.jar -p 4444 -h localhost -i $1 -d -s $2
fi
