var top 		= require('./lib/top-push-client'),
	client 		= top.client,
	uri 		= process.argv[2],
    connects 	= parseInt(process.argv[3]),
	flag		= 'receiver',
	MessageType = { PUBLISH: 1, PUBCONFIRM: 2 };

var i = 0;
function doConnect() {
    if(i++ == connects) return;
    
    client(flag, 'ws://localhost:8080/backend').
    	on('connect', function(context) {}).
    	on('message', function(context) {
			var msg = context.message;
			
			if(context.messageType == MessageType.PUBLISH) {
				//console.log('---- receive publish ----');
				//console.log(msg);
				context.reply(MessageType.PUBCONFIRM, { MessageId: msg.MessageId });
			}
		});

    setTimeout(doConnect, 100);
}

doConnect();
