#!/bin/bash

(
    echo "Starting " + $1 + $2 "..." >&2

    nohup java -jar ../processes/$1-$2-SNAPSHOT-jar-with-dependencies.jar -jndiProperties /guicejndi.properties > /dev/null;

) &

