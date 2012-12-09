var fork = require('child_process').fork,
    WebSocketClient = require('websocket').client,
    client = new WebSocketClient(),
    count = 0,
    match = 0,
    count_connect_fail = 0;
    begin = null,
    uri = process.argv[2],
    total = parseInt(process.argv[3]),
    MSG = '';

//100 byte
//1k
//4k
for(var i = 0; i < 100; i++)
    MSG += 'i';

client.on('connectFailed', function(error) {
    count_connect_fail += 1;
    //socket Hang up
    console.log('Connect Failed: ' + error.toString());
    sendAndExit(error);
});

client.on('connect', function(connection) {
    console.log('WebSocket client connected');

    connection.on('error', function(error) {
        console.log("Connection Error: " + error.toString());
        sendAndExit(error);
    });
    connection.on('close', function() {
        console.log('Connection Closed');
        process.exit();
    });
    
    for(var i = 0;i < 4; i++){
        var child = fork('simpleEnhanceChild.js', []);
        console.log(child);
        child.send(connection);
    }

    //tell server start push
    if (connection.connected) {
        connection.sendUTF(total);
        connection.sendUTF(MSG);
        begin = new Date();
        console.log('total: %s', total);
    }
});

client.connect(uri);
