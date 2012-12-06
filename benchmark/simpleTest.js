var WebSocketClient = require('websocket').client,
    client = new WebSocketClient(),
    count = 0,
    match = 0,
    count_connect_fail = 0;
    begin = null,
    uri = process.argv[2],
    total = parseInt(process.argv[3]),
    MSG = '';

//client.config.websocketVersion = 8;
//console.log(process.argv);

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
    connection.on('message', function(message) {
        if (message.type === 'utf8') {
            var msg = message.utf8Data;
            count++;
            if(msg == MSG) match++;
            //console.log('count: %s', count);
            if(count == total) {
                connection.close();

                var cost = new Date() - begin;
                console.log('cost: %s ms', cost);
                console.log('avg: %s msg/s', (total / cost) * 1000);
                console.log('match: %s', match);
                console.log('count_connect_fail: %s', count_connect_fail);
            
                sendAndExit('done');
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

function sendAndExit(e){
    if(process.send)
        process.send(e);
    process.exit();
}

//100 byte
//1k
//4k
for(var i = 0; i < 100; i++)
    MSG += 'i';

if(!isNaN(total)){
    client.connect(uri);
}
