var WebSocketClient = require('websocket').client,
    client = new WebSocketClient(),
    count = 0,
    match = 0,
    begin = null,
    uri = process.argv[2],
    total = parseInt(process.argv[3]),
    MSG = '';

//client.config.websocketVersion = 8;
console.log(process.argv);

client.on('connectFailed', function(error) {
    console.log('Connect Failed: ' + error.toString());
    process.exit();
});

client.on('connect', function(connection) {
    console.log('WebSocket client connected');
    connection.on('error', function(error) {
        console.log("Connection Error: " + error.toString());
        process.exit();
    });
    connection.on('close', function() {
        console.log('echo-protocol Connection Closed');
        process.exit();
    });
    connection.on('message', function(message) {
        if (message.type === 'utf8') {
            var msg = message.utf8Data;
            count++;
            if(msg == MSG) match++;
            //console.log('count: %s', count);
            if(count == total) {
                var cost = new Date() - begin;
                console.log('cost: %s ms', cost);
                console.log('avg: %s msg/s', (total / cost) * 1000);
                console.log('match: %s', match);
                connection.close();
                process.send('done');
                process.exit();
            } 
        }
    });
    
    //tell server start push
    if (connection.connected) {
        connection.sendUTF(total);
        connection.sendUTF(MSG);
        begin = new Date();
        console.log('total: %s', total);
    }
});

//100 byte
for(var i = 0; i < 100; i++)
    MSG += 'i';

if(!isNaN(total)){
    client.connect(uri);
}
process.on('message', function(m) {
    var i = parseInt(m);

    if(isNaN(i))
        total = i;
    else if(!isNaN(i))
        uri = m;
    else if(m == 'start')
        client.connect(uri);
});
