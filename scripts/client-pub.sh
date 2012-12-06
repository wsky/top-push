#!/bin/bash
source var.sh
cd ..

ssh -i $KEY_PAIRS $SERVER "cd ~/;rm -rf tests;mkdir tests"
scp -i $KEY_PAIRS -r tests/nodejs $SERVER:~/tests/nodejs

ssh -i $KEY_PAIRS $CLIENT_01 "cd ~/;rm -rf tests;mkdir tests"
scp -i $KEY_PAIRS -r tests/nodejs $CLIENT_01:~/tests/nodejs