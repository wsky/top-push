#!/bin/bash
source var.sh

ssh -i $KEY_PAIRS -o "StrictHostKeyChecking no" ${CLIENTS[$2]} "cd ~;rm -rf tests;mkdir tests"