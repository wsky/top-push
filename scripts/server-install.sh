#jetty
JETTY_VERSION=8.1.7.v20120910
rm -rf jetty*
#wget http://download.eclipse.org/jetty/$JETTY_VERSION/dist/jetty-distribution-$JETTY_VERSION.tar.gz
wget http://archive.eclipse.org/jetty/$JETTY_VERSION/dist/jetty-distribution-$JETTY_VERSION.tar.gz
tar xfz jetty-distribution-$JETTY_VERSION.tar.gz
