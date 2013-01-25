# Testcases on AWS

## install

change servers in var.h, then

```shell
sudo sh aws-install.sh
sudo sh server-pub.sh
sudo sh client-pub.sh
```

## server

```shell
killall -9 java;cd ~/$JETTY_DIR;java -Xmx4096m -server -jar start.jar &
```

## run-clients


run receivers

```shell
node index.js receiver.js 2 ws://10.132.145.219:8080/push/frontend 1 receive1
node index.js receiver.js 2 ws://10.132.145.219:8080/push/frontend 1 receive2
```

```shell
node sender.js ws://10.132.145.219:8080/push/backend receive1 receive2
```