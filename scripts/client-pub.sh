#!/bin/bash
source var.sh

for i in "${CLIENTS[@]}"
do
	ssh -i $KEY_PAIRS -o "StrictHostKeyChecking no" $i "cd ~;rm -rf tests;mkdir tests"
	scp -i $KEY_PAIRS -r ../tests/nodejs $i:~/tests/nodejs
done