#!/bin/bash

# script configuration
URL=46.4.89.183
USER=root
VERSION=1.3-SNAPSHOT
BASEPATH=/root
PROCESSES="listeners/facebook/facebook-process resolver/process dispatcher realtime-profiler/realtime-profiler-process filter/filter-process indexer"

#functions
function build()
{
	PROCESS=$1
	echo === building $PROCESS ===
	mvn -o -DskipTests -pl $PROCESS assembly:assembly >> build.log
	echo === $PROCESS build completed ===
}

function deploy()
{
	PROCESS=$1
	echo === deploying $PROCESS ===
	scp $PROCESS/target/*-$VERSION-jar-with-dependencies.jar $USER@$URL:$BASEPATH/processes/
	echo === $PROCESS deploy completed ===
}

function stop()
{
	echo === stopping all the processes remotely ===
	PIDS=$(ssh $USER@$URL ps -ef | grep -E "*-$VERSION-jar-with-dependencies.jar" | awk '{print $2}')
	echo === going to kill $PIDS ===
	for PID in $PIDS
	do
		COMMAND="kill $PID"
		echo === killing $PID ===
		ssh $USER@$URL $COMMAND
		echo === $PID killed ===
	done
	echo === all remote processes stopped ===
}

function start()
{
	echo === starting all the processes remotely ===
	FILES=$(ssh $USER@$URL ls $BASEPATH/processes/)
	for FILE in $FILES
	do
		COMMAND="nohup java -jar $BASEPATH/processes/$FILE -jndiProperties /guicejndi.properties >> $BASEPATH/processes/$FILE.nohup.log &"
		echo === starting $FILE ===
		ssh $USER@$URL $COMMAND & > /dev/null
		echo === $FILE started ===
	done
	echo === all remote processes started ===
}

STARTED=$(date)
echo === deploy started at $STARTED
echo === installing dependencies ===
mvn -o -DskipTests clean install > install.log
echo === dependencies installed ===
echo === deploying on $URL components ===
stop
for PROCESS in $PROCESSES
do
	build $PROCESS 
	deploy $PROCESS
done
start
echo === deploy on $URL successful ===
echo === re-deploying REST APIs ===
mvn -o -DskipTests -pl platform clean package cargo:deploy > rest.log
echo === APIs redeployed ===
ENDED=$(date)
echo === deploy ended at $ENDED