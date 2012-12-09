/*
    test multi-connects to ws-frontend in single process
    will ping
*/

var fork = require('child_process').fork,
    websocket = require('websocket').client,
	uri = process.argv[2],
    connects = parseInt(process.argv[3]),
    mode = process.argv[4],
    connect_fail = 0,
    connect_sucess = 0,
    count = 0,
    count_match = 0;
    MSG = ''
    SIZE = 1024 * 64,//msg size by machine tcp buffer size
    INTERVAL = 15000;//ping server interval
for(var i = 0; i < SIZE; i++)
    MSG += 'i';


function connect() {
    var client = new websocket({
        //https://github.com/Worlize/WebSocket-Node/blob/master/lib/WebSocketClient.js
        //default is 16k
        fragmentationThreshold: SIZE
    });

    client.on('connectFailed', function(error) { 
        console.log('%s Connect Failed: %s, Success:%s', ++connect_fail, error.toString(), connect_sucess); 
    });

    client.on('connect', function(connection) {
        console.log('%s WebSocket client connected, Failed:%s', ++connect_sucess, connect_fail);
        var begin = new Date();
        connection.on('error', function(error) { console.log("Connection Error: " + error.toString()); });
        connection.on('close', function() { console.log('Connection Closed'); });
        connection.on('message', function(message) {
            if (message.type === 'utf8') {
                count++;
                var msg = message.utf8Data;
                if(msg == MSG) count_match++;
                if(count % 10000 == 0)
                    console.log('%s messages received, matchs %s', 
                        count, 
                        count_match); 
            } else {
                console.log(message);
            }
        });

        setInterval(function() {
            //ping can carry more data
            //default 1 byte
            connection.ping();
        }, INTERVAL);
    });

    client.connect(uri);
}
var i = 0;
function doConnect() {
    if(i++ == connects) return;
    connect();
    setTimeout(doConnect, 10);
}
function doParallelConnect() {
    for(var j = 0;j < connects; j++)
        connect();
}
function doParallelConnect2() {
    for(var j = 0;j < 100; j++) {
        if(i == connects) return;
        connect();
        i++;
    }
    setTimeout(doParallelConnect2, 20);
}

if(mode == 0)
    doConnect();
else if(mode == 1)
    doParallelConnect();
else if(mode == 2)
    doParallelConnect2();


