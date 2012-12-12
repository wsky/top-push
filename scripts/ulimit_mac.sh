sysctl kern.maxfiles
#kern.maxfiles: 12288
sysctl kern.maxfilesperproc
#kern.maxfilesperproc: 10240
sudo sysctl -w kern.maxfiles=1048600
#kern.maxfiles: 12288 -> 1048600
sudo sysctl -w kern.maxfilesperproc=1048576
#kern.maxfilesperproc: 10240 -> 1048576

sudo sysctl -w net.inet.tcp.rfc1323=1
#must bigger than sendspace
sudo sysctl -w kern.ipc.maxsockbuf=6291456
sudo sysctl -w net.inet.tcp.sendspace=4194304
#65536 -> 16777216
#result too large
sudo sysctl -w net.inet.tcp.recvspace=4194304
#65536 -> 16777216


sudo sysctl -w kern.ipc.somaxconn=512
sudo sysctl -w kern.ipc.maxsockets=2048
sudo sysctl -w kern.ipc.nmbclusters=2048
sudo sysctl -w net.inet.tcp.win_scale_factor=3
sudo sysctl -w net.inet.tcp.sockthreshold=16
#sudo sysctl -w net.inet.tcp.sendspace=262144
#sudo sysctl -w net.inet.tcp.recvspace=262144
sudo sysctl -w net.inet.tcp.mssdflt=1440
sudo sysctl -w net.inet.tcp.msl=15000
sudo sysctl -w net.inet.tcp.always_keepalive=0
sudo sysctl -w net.inet.tcp.delayed_ack=0
sudo sysctl -w net.inet.tcp.slowstart_flightsize=4
sudo sysctl -w net.inet.tcp.blackhole=2
sudo sysctl -w net.inet.udp.blackhole=1
sudo sysctl -w net.inet.icmp.icmplim=50

ulimit -n
#256
sudo ulimit -SHn 1048576
ulimit -n