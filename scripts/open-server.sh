#!/bin/bash
source var.sh


ssh -i $KEY_PAIRS -o "StrictHostKeyChecking no" ${SERVERS[$1]}

#java -Xmx4096m -jar start.jar &