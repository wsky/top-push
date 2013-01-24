/*
    (The MIT License)

    Copyright (C) 2012 wsky (wskyhx at gmail.com) and other contributors

    Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

/*

//message format
var message={
	messageType: 1,
	from: '', //receiving
	to: '', //sending
	bodyFormat: 0,
	remainingLength: 100,
	body: {}
}
*/

var	EventEmitter 	= require('events').EventEmitter,
	websocket 		= require('websocket').client,
	PING_INTERVAL 	= 60000,
	SIZE_MSG 		= 1024,
	PROTOCOL 		= 'default', //nodejs version do not impl other protocol
	ENCODING 		= 'utf-8';

module.exports.client = function(flag, uri) {
	var e = endpoint(flag);
	var conn;

	ws(
		function(connection) {
			conn = connection;
			e.emit('connect', { sendMessage: send });
		},
		function(message) {
			if(message.type == 'binary') {
				var msg = readMessage(message.binaryData);
				e.emit('message', {
					messageType: msg.messageType,
					message: msg.body,
					reply: function(messageType, messageBody) {
						send(msg.from, messageType, messageBody);
					}
				});
			}
		}
	).connect(uri, PROTOCOL, flag);

	function send(to, messageType, messageBody) {
		conn.sendMessage(writeMessage({
			messageType: messageType,
			to: to,
			body: messageBody
		}, getBuffer()));
	}

	return e;
}


function endpoint(id) {
	var e = { 
		id: id, 
		emiter: new EventEmitter() 
	};
	e.emit = function(event, argument) { this.emiter.emit(event, argument); return this;};
	e.on = function(event, callback) { this.emiter.on(event, callback); return this;};
	e.once = function(event, callback) { this.emiter.once(event, callback); return this; };
	return e;
}

function ws(onConnect, onMessage, onConnectFailed, onError, onClose) {
	var client = new websocket({
    	//https://github.com/Worlize/WebSocket-Node/blob/master/lib/WebSocketClient.js
    	//default is 16k
		//fragmentationThreshold: SIZE_MSG
		//websocketVersion: 8
	});

    client.on('connectFailed', function(error) { 
        console.log('Connect Failed: %s', error.toString());
        if(onConnectFailed)
        	onConnectFailed(error);
    });

    client.on('connect', function(connection) {
        console.log('WebSocket client connected');

        connection.on('error', function(error) {
        	stopPing();
        	console.log("Connection Error: %s", error.toString()); 
        	if(onError) onError(error);
        });
        
        connection.on('close', function(closeCode, closeDescription) { 
        	stopPing();
        	console.log("Connection Close: %s - %s", closeCode, closeDescription); 
        	if(onClose) onClose(closeCode, closeDescription);
        });

        connection.on('message', function(message) {
        	doPing();

    		if(onMessage)
    			onMessage(message);
        });

        if(onConnect) {
        	connection.sendMessage = function(data) {
        		doPing();
        		connection.sendBytes(data);
        	}
        	onConnect(connection);
        }

		var timer;
	    function doPing() {
	    	stopPing();
	    	timer = setTimeout(function() {
	    		try {
					connection.ping();
	    		} catch(e) {
					console.log(e);
	    		}
	        	doPing();
	    	}, PING_INTERVAL);
	    }
	    function stopPing() {
			if(timer)
				clearTimeout(timer);
	    }
        doPing();
    });

    return client;
}

//TODO:buffer pool
function getBuffer() {
	return new Buffer(SIZE_MSG);
}
function returnBuffer() {
	
}
//message protocol refer to
//https://github.com/wsky/top-push/issues/13
//0,1-8,9,10-13,14-N
function writeMessage(message, buffer) {
	var type = new Buffer([message.messageType]);
	var to = new Buffer(padLeft(message.to, 8), ENCODING);
	var bodyFormat = new Buffer([0]);
	var body = new Buffer(JSON.stringify(message.body), ENCODING);
	message.remainingLength = body.length;

	type.copy(buffer, 0, 0, 1);  	
	to.copy(buffer, 1, 0); 	
  	bodyFormat.copy(buffer, 9, 0, 1);
	//BE 00 00 00 28 
	//LE 28 00 00 00
	buffer.writeInt32BE(message.remainingLength, 10);
	body.copy(buffer, 14, 0, message.remainingLength);
	return buffer;
}
function readMessage(buffer) {
	var msg = {};
	msg.messageType = buffer[0];
	msg.from = buffer.toString(ENCODING, 1, 9);  	
	msg.bodyFormat = buffer[9];
	msg.remainingLength = buffer.readInt32BE(10);
	msg.body = JSON.parse(buffer.toString(ENCODING, 14, 14 + msg.remainingLength));
	return msg;
}
function padLeft(str, totalWidth) {
	if(str.length >= totalWidth)   	
    	return str;
  	var prefix = '';
  	for(i = 1; i <= totalWidth - this.length; i++)
    	prefix += ' ';
  	return prefix + str;	  	
}
//tests

