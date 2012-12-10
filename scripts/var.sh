#!/bin/bash
JETTY_VERSION=8.1.7.v20120910

KEY_PAIRS=~/Documents/test-cluster.pem

BRIDGE=ubuntu@ec2-46-51-227-180.ap-northeast-1.compute.amazonaws.com

SERVERS=(
	ubuntu@ec2-54-248-186-209.ap-northeast-1.compute.amazonaws.com
	ec2-user@ec2-175-41-227-119.ap-northeast-1.compute.amazonaws.com
)

CLIENTS=(
	#m1.xlarge
	#m1.small
	ubuntu@ec2-54-248-179-61.ap-northeast-1.compute.amazonaws.com
	ubuntu@ec2-54-248-28-76.ap-northeast-1.compute.amazonaws.com
	ubuntu@ec2-54-248-157-51.ap-northeast-1.compute.amazonaws.com
	ubuntu@ec2-54-248-186-114.ap-northeast-1.compute.amazonaws.com
)