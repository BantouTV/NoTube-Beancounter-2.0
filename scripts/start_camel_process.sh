#!/bin/bash

echo "Starting: " $1 " and storing the output to " ${1/-SNAPSHOT-jar-with-dependencies.jar/.out}

nohup java -server -Xmx1024m -Xms1024m -jar $1 -jndiProperties /guicejndi.properties > ${1/-SNAPSHOT-jar-with-dependencies.jar/.out}  &
