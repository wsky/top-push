#tuning
#http://wiki.eclipse.org/Jetty/Howto/High_Load
#need root
echo "ulimit -SHn 102400"  >> /etc/rc.load 
echo "ulimit -SHn 102400"  >> /etc/profile
ulimit -SHn 102400

#TCP Buffer Sizes
sysctl -w net.core.rmem_max=16777216
sysctl -w net.core.wmem_max=16777216
sysctl -w net.ipv4.tcp_rmem="4096 87380 16777216"
sysctl -w net.ipv4.tcp_wmem="4096 16384 16777216"
#queue size
sysctl -w net.core.somaxconn=4096
#for upper-layer (java) processing
sysctl -w net.core.netdev_max_backlog=16384
sysctl -w net.ipv4.tcp_max_syn_backlog=8192
sysctl -w net.ipv4.tcp_syncookies=1
#ports
sysctl -w net.ipv4.ip_local_port_range="1024 65535"
sysctl -w net.ipv4.tcp_tw_recycle=1
#Congestion Control
sysctl -w net.ipv4.tcp_congestion_control=cubic

#if ubuntu
sudo apt-get update
sudo apt-get install -y openjdk-6-jre-headless
sudo apt-get install -y iftop
sudo apt-get install -y iperf