var base = require('./base'),
    VAR = base.var(),
    ws = base.ws,

    count = 0,
    count_match = 0;
    begin = null,

	uri = process.argv[2],
	total = parseInt(process.argv[3]);

ws(
    function(connection){
        begin = new Date();
        connection.sendUTF(total);
        connection.sendUTF(VAR.MSG);
        return;

        //client send will slow, avoid this currently.
        for(var i = 0; i < total; i++)
            connection.sendUTF(VAR.MSG);
        
        var cost = new Date() - begin;
        console.log('cost: %s ms', cost);
        console.log('avg: %s msg/s', (total / cost) * 1000);

    },
    function(message) {
        if (message.type === 'utf8') {
            count++;

            var msg = message.utf8Data;
            if(msg == VAR.MSG_CONFIRM) count_match++;
            
            if(count % 10000 == 0) {
                var delay = new Date() - begin;
                console.log('%s messages received, matchs %s, delay: %sms, avg:%smessages/s', 
                    count, 
                    count_match,
                    delay,
                    count * 1000 / delay);
            }
        } else {
            console.log(message);
        }
    },
    null).
connect(uri);
