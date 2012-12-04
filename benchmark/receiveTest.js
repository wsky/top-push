var WebSocketClient = require('websocket').client,
    client = new WebSocketClient(),
    total = 0,
    count = 0,
    match = 0,
    begin = null,
    MSG = "hello";

client.on('connectFailed', function(error) {
    console.log('Connect Failed: ' + error.toString());
});

client.on('connect', function(connection) {
    console.log('WebSocket client connected');
    connection.on('error', function(error) {
        console.log("Connection Error: " + error.toString());
    });
    connection.on('close', function() {
        console.log('echo-protocol Connection Closed');
    });
    connection.on('message', function(message) {
        if (message.type === 'utf8') {
            var msg = message.utf8Data;

            //console.log("Received: '" + msg + "'");

            if(total == 0) {
                console.log('total: %s', total = parseInt(msg));
                begin = new Date();
                return;
            }
            count++;
            if(msg == MSG) match++;
            //console.log('count: %s', count);
            if(count == total) {
                var cost = new Date() - begin;
                console.log('cost: %s ms', cost);
                console.log('avg: %s msg/s', (total / cost) * 1000);
                console.log('match: %s', match);
                process.exit();
            } 
        }
    });

    function sendNumber() {
        if (connection.connected) {
            var number = Math.round(Math.random() * 0xFFFFFF);
            connection.sendUTF(number.toString());
            setTimeout(sendNumber, 1000);
        }
    }
    //sendNumber();

    //tell server start push
    if (connection.connected)        
        connection.sendUTF(MSG);
});

for(var i = 0; i < 100; i++)
    MSG += 'i';


client.connect('ws://localhost:9090');