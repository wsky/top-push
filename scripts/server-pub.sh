#!/bin/bash
source var.sh

JETTY_DIR=jetty-distribution-$JETTY_VERSION

#build package first
cd ..
mvn package
cd scripts

scp -i $KEY_PAIRS ../target/top-push-1.0-SNAPSHOT.war $SERVER:~/$JETTY_DIR/webapps/top-push-1.0-SNAPSHOT.war
scp -i $KEY_PAIRS top-push.xml $SERVER:~/$JETTY_DIR/contexts/top-push.xml

#run
ssh -i $KEY_PAIRS $SERVER "killall -9 java;cd ~/$JETTY_DIR;java -Xmx4096m -jar start.jar"