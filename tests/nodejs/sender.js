/*
    (The MIT License)

    Copyright (C) 2012 wsky (wskyhx at gmail.com) and other contributors

    Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

var top 		= require('./lib/top-push-client'),
	client 		= top.client,
	uri 		= process.argv[2],
	to 			= process.argv[3],
	MessageType = { PUBLISH: 1, PUBCONFIRM: 2 };


client('sender', uri).
	on('connect', function(context) {
		var msg = { MessageId: "20130104" };
		console.log(msg);
		console.log(to);
		setTimeout(function(){
			setInterval(function(){
				for(var i = 0; i < 100; i++)
					context.sendMessage(to, MessageType.PUBLISH, msg);
			}, 10);
		}, 2000);
	}).
	on('message', function(context) {
		/*var msg = context.message;

		if(context.messageType == MessageType.PUBCONFIRM) {
			//console.log('---- receive confirm ----');
			//console.log(msg);
			//process.exit();
		}*/
	});

process.openStdin().addListener("data", function(d) { process.exit(); });


