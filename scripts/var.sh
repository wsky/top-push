#!/bin/bash
JETTY_VERSION=8.1.7.v20120910

KEY_PAIRS=/Users/houkun/Documents/test-cluster.pem

BRIDGE=ubuntu@ec2-46-51-227-180.ap-northeast-1.compute.amazonaws.com

SERVERS=(
	#m1.large
	#ubuntu@ec2-176-34-6-67.ap-northeast-1.compute.amazonaws.com
	ubuntu@ec2-54-248-202-247.ap-northeast-1.compute.amazonaws.com
	ubuntu@ec2-54-248-19-131.ap-northeast-1.compute.amazonaws.com
	ubuntu@ec2-54-248-146-39.ap-northeast-1.compute.amazonaws.com
)

CLIENTS=(
	#m1.small
	#ubuntu@ec2-54-248-153-48.ap-northeast-1.compute.amazonaws.com
	#ubuntu@ec2-54-248-171-69.ap-northeast-1.compute.amazonaws.com
	#ubuntu@ec2-54-248-168-205.ap-northeast-1.compute.amazonaws.com
	#ubuntu@ec2-54-248-199-254.ap-northeast-1.compute.amazonaws.com
	#m1.large
	#ubuntu@ec2-176-34-6-67.ap-northeast-1.compute.amazonaws.com
	#m1.xlarge
	ubuntu@ec2-54-248-202-247.ap-northeast-1.compute.amazonaws.com
	ubuntu@ec2-54-248-19-131.ap-northeast-1.compute.amazonaws.com
	ubuntu@ec2-54-248-146-39.ap-northeast-1.compute.amazonaws.com
)