#!/bin/bash
source var.sh


ssh -i $KEY_PAIRS -o "StrictHostKeyChecking no" ${SERVERS[$1]}