#tuning
#need root
#echo "ulimit -SHn 102400"  >> /etc/rc.load 
#echo "ulimit -SHn 102400"  >> /etc/profile
#ulimit -SHn 102400

#if ubuntu
sudo apt-get update
sudo apt-get install -y openjdk-6-jre-headless
sudo apt-get install -y iftop