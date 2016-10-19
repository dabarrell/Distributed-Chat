#!/bin/bash

if mvn clean package; then
  # gnome-terminal --window-with-profile=hold -t "dev" -e 'java -jar -Dtinylog.writer=console -Dtinylog.writer.filename=log.txt -Dtinylog.level=trace target/ChatServer-1.0-jar-with-dependencies.jar -l "server.conf" -n "dev"' &
  # gnome-terminal --window-with-profile=hold -t "dev2" -e 'java -jar -Dtinylog.writer=console -Dtinylog.writer.filename=log.txt -Dtinylog.level=trace target/ChatServer-1.0-jar-with-dependencies.jar -l "server.conf" -n "dev2"' &
  # sleep 4
  # gnome-terminal --window-with-profile=hold -t "dev2" -e 'java -jar -Dtinylog.writer=console -Dtinylog.writer.filename=log.txt -Dtinylog.level=trace target/ChatServer-1.0-jar-with-dependencies.jar -l "server2.conf" -n "dev3" --add -v ' &
  java -jar -Dtinylog.writer=console -Dtinylog.writer.filename=log.txt -Dtinylog.level=trace target/ChatServer-1.0-jar-with-dependencies.jar -l "servers_remote.conf" -n "dev3" -a -v
fi
