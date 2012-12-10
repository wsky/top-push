#nodejs
NODEJS_TAR=node-v0.8.15-linux-x64.tar.gz
NODEJS_DIR=node-v0.8.15-linux-x64
rm -rf node*
wget http://nodejs.org/dist/v0.8.15/$NODEJS_TAR
tar zxvf $NODEJS_TAR
mv $NODEJS_DIR nodejs
sudo ln nodejs/bin/node /usr/bin/node

