sysctl kern.maxfiles
#kern.maxfiles: 12288
sysctl kern.maxfilesperproc
#kern.maxfilesperproc: 10240
sudo sysctl -w kern.maxfiles=1048600
#kern.maxfiles: 12288 -> 1048600
sudo sysctl -w kern.maxfilesperproc=1048576
#kern.maxfilesperproc: 10240 -> 1048576
ulimit -n
#256
ulimit -n 1048576
ulimit -n