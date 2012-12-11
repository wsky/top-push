var websocket = require('websocket').client,
	SIZE_MSG = 1024,//msg size by machine tcp buffer size, like 64K/128K, 1K in product
	SIZE_MSG_CONFIRM = 8,//8 Byte for long
	VAR = init(),
	connect_fail = 0,
    connect_sucess = 0,
	count = 0,
    count_match = 0;

function init() {
	var r = {
		MSG: '',
		MSG_CONFIRM: '',
		PING_INTERVAL: 60000,//adjust to server idle time
		WS_CONFIG: {
        	//https://github.com/Worlize/WebSocket-Node/blob/master/lib/WebSocketClient.js
        	//default is 16k
			fragmentationThreshold: SIZE_MSG
			//client.config.websocketVersion = 8;
		}
	};
	
	for(var i = 0; i < SIZE_MSG; i++)
    	r.MSG += 'i';
    for(var i = 0; i < SIZE_MSG_CONFIRM; i++)
    	r.MSG_CONFIRM += 'i';


    console.log(r);

    return r;
}

module.exports.var = function() { return VAR;}

module.exports.ws = function(onConnect, onMessage, ping, confirm) {
	var client = new websocket(VAR.WS_CONFIG);

    client.on('connectFailed', function(error) { 
        console.log('%s Connect Failed: %s, Success:%s', ++connect_fail, error.toString(), connect_sucess); 
    });

    client.on('connect', function(connection) {
        console.log('%s WebSocket client connected, Failed:%s', ++connect_sucess, connect_fail);

        connection.on('error', function(error) { 
        	console.log("Connection Error: %s", error.toString()); 
        });
        connection.on('close', function() { 
        	console.log('Connection Closed'); 
    	});
        connection.on('message', function(message) {
    		if(onMessage)
    			onMessage(message);
    		else {
    			if (message.type === 'utf8') {
	                count++;

	                var msg = message.utf8Data;
	                if(msg == VAR.MSG || msg == VAR.MSG_CONFIRM) count_match++;
	                
	                if(count % 10000 == 0)
	                    console.log('%s messages received, matchs %s', 
	                        count, 
	                        count_match); 
	            } else {
	                console.log(message);
	            }
    		}


    		if(confirm)
    			connection.sendUTF(VAR.MSG_CONFIRM);
        });

        if(onConnect)
        	onConnect(connection);

        if(ping)
        	setInterval(function() {
            	//ping can carry more data
            	//default 1 byte
            	connection.ping();
        	}, VAR.PING_INTERVAL);
    });

    return client;
}