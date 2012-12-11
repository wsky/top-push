#!/bin/bash
source var.sh

#servers
for i in "${SERVERS[@]}"
do
	echo $i
	scp -i $KEY_PAIRS -o "StrictHostKeyChecking no" base-install.sh $i:~/base-install.sh
	scp -i $KEY_PAIRS -o "StrictHostKeyChecking no" server-install.sh $i:~/server-install.sh
	ssh -i $KEY_PAIRS -o "StrictHostKeyChecking no" $i "cd ~;sh base-install.sh;sh server-install.sh;"
done

#clients
for i in "${CLIENTS[@]}"
do
	echo $i
	scp -i $KEY_PAIRS -o "StrictHostKeyChecking no" base-install.sh $i:~/base-install.sh
	scp -i $KEY_PAIRS -o "StrictHostKeyChecking no" client-install.sh $i:~/client-install.sh
	ssh -i $KEY_PAIRS -o "StrictHostKeyChecking no" $i "cd ~;sh base-install.sh;sh client-install.sh"
done
