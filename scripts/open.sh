#!/bin/bash
source var.sh

servers=(
	#server 0
	ubuntu@ec2-175-41-213-43.ap-northeast-1.compute.amazonaws.com
	#client01
	ec2-user@ec2-54-248-133-156.ap-northeast-1.compute.amazonaws.com
	#client02
	ubuntu@ec2-54-248-194-17.ap-northeast-1.compute.amazonaws.com
)
echo ${servers[$1]}
#osascript -e 'tell application "Terminal" to activate' -e 'tell application "System Events" to tell process "Terminal" to keystroke "t" using command down'
sudo ssh -i $KEY_PAIRS ${servers[$1]}

cd /home/ec2-user/tests/nodejs/
cd /home/ubuntu/tests/nodejs/
node frontend2.js ws://10.160.130.79:8080/push/frontend 10000 1
