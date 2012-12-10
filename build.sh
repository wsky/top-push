source scripts/var.sh

rm -rf build
mkdir build

mvn package

scp -i $KEY_PAIRS -r target/top-push-1.0-SNAPSHOT.war $BRIDGE:~/top-push-1.0-SNAPSHOT.war
scp -i $KEY_PAIRS -r scripts $BRIDGE:~/scripts
scp -i $KEY_PAIRS -r tests $BRIDGE:~/tests

ssh -i $KEY_PAIRS $BRIDGE
