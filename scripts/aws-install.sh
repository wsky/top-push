#!/bin/bash
source var.sh

#server
#copy
scp -i $KEY_PAIRS server-install.sh $SERVER:~/server-install.sh
#install
#if you need
ssh -i $KEY_PAIRS $SERVER "cd ~;sh server-install.sh"

#clients
#copy
scp -i $KEY_PAIRS client-install.sh $CLIENT_01:~/client-install.sh
ssh -i $KEY_PAIRS $CLIENT_01 "cd ~;sh client-install.sh"
