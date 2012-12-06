#!/bin/bash
source var.sh

ssh -i $KEY_PAIRS $SERVER "cd ~/;rm -rf tests"
scp -i $KEY_PAIRS ../tests/nodejs $CLIENT_01:~/tests/nodejs