var fork = require('child_process').fork,
    websocket = require('websocket').client,
	SIZE = 1024 * 64,
    client = new WebSocketClient({ fragmentationThreshold: SIZE }),
	uri = process.argv[2] ? process.argv[2] : 'ws://localhost:9090/frontend',
    total = parseInt(process.argv[3]),
    fork_count = parseInt(process.argv[4]),
    isChild = process.argv[5],
    childIndex = process.argv[6],
    count = 0,
    count_match = 0;
    MSG = '';

for(var i = 0; i < SIZE; i++)
    MSG += 'i';

client.on('connectFailed', function(error) { console.log('Connect Failed: ' + error.toString()); });

client.on('connect', function(connection) {
    console.log('WebSocket client connected, wait for %s messages', total);

    var begin = new Date();

    connection.on('error', function(error) { console.log("Connection Error: " + error.toString()); });
    connection.on('close', function() { console.log('Connection Closed'); });
    connection.on('message', function(message) {
        if (message.type === 'utf8') {
            if(count == 0) 
                begin = new Date();
            count++;
            if(message.utf8Data == MSG) count_match++;
            if(count != total) return;
            console.log('#%s %s messages received in %sms, matchs %s', 
                childIndex, 
                count, 
                new Date() - begin, 
                count_match);
            if(isChild)
                process.exit();
        }
    });
});

client.connect(uri);

var i = 0;
function doFork() {
    if(++i == fork_count) return;
    fork('frontend.js', [uri, total, fork_count, true, i]);
    setTimeout(doFork, 10);
}

if(!isChild) {
    doFork();
}