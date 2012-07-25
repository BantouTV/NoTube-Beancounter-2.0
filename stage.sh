#!/bin/bash

# script configuration
URL=46.4.89.183
USER=root
VERSION=1.1-SNAPSHOT
BASEPATH=/root/
INDEXER=indexer
RESOLVERPATH=resolver/
FACEBOOKPATH=facebook/

# functions
function changeDir() {
  TARGET=$1
  cd $TARGET
}

# check if there are no running processes on the staging machine
# if yes, kill them

# build
echo === building beancounter.io ===
# exec mvn clean install > /dev/null
# exec mvn clean > /dev/null
echo === build completed ===
echo === stopping remote processes ===
echo === remote processes stopped ===
echo === deploying component: $INDEXER ===
exec changeDir -f $INDEXER/
exec mvn clean assembly:assembly
exec scp target/$INDEXER-$VERSION-jar-with-dependencies.jar USER@URL:$BASEPATH$INDEXER/
# echo === starting component: $INDEXER ===
# exec ssh USER@URL nohup java -jar $INDEXER-$INDEXER-$VERSION-jar-with-dependencies.jar -jndiProperties /guicejndi.properties
# echo == component $INDEXER deployed and started ===
