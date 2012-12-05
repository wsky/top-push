var websocket = require('websocket').client,
	client = new websocket();
	MSG = ''
	uri = process.argv[2] ? process.argv[2] : 'ws://localhost:9090/backend',
	total = process.argv[3] ? parseInt(process.argv[3]) : 10000;

console.log(process.argv);

//100 byte
for(var i = 0; i < 100; i++)
    MSG += 'i';

client.on('connectFailed', function(error) { console.log('Connect Failed: ' + error.toString()); });

client.on('connect', function(connection) {
    connection.on('error', function(error) { console.log("Connection Error: " + error.toString()); });
    connection.on('close', function() { console.log('Connection Closed'); });
    connection.on('message', function(message) {
        if (message.type === 'utf8') {}
    });
	
	var begin = new Date();

	for(var i = 0; i < total; i++)
 		connection.sendUTF(MSG);
    
    var cost = new Date() - begin;
    console.log('cost: %s ms', cost);
    console.log('avg: %s msg/s', (total / cost) * 1000);
    //must wait all message send out.
    //process.exit();
});

client.connect(uri);
