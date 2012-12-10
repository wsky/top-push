#!/bin/bash
source var.sh


ssh -i $KEY_PAIRS -o "StrictHostKeyChecking no" ${CLIENTS[$1]}

#echo "ulimit -SHn 102400"  >> /etc/rc.load 
#echo "ulimit -SHn 102400"  >> /etc/profile
#ulimit -SHn 102400
#cd /home/ubuntu/tests/nodejs/
#node frontend2.js ws://10.150.141.17:8080/push/frontend 20000 2