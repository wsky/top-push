var base = require('./base'),
    VAR = base.var(),
    ws = base.ws,

    count = 0,
    match = 0,
    begin = null,

    uri = process.argv[2],
    total = parseInt(process.argv[3]);

function sendAndExit(e){
    if(process.send)
        process.send(e);
    process.exit();
}


ws(
    function(connection){
        if (connection.connected) {
            connection.sendUTF(total);
            connection.sendUTF(VAR.MSG);
            begin = new Date();
            console.log('total: %s', total);
        }
    },
    function(message){
        if (message.type === 'utf8') {
            var msg = message.utf8Data;
            count++;
            if(msg == VAR.MSG) match++;
            if(count == total) {
                var cost = new Date() - begin;
                console.log('cost: %s ms', cost);
                console.log('avg: %s msg/s', (total / cost) * 1000);
                console.log('match: %s', match);
            
                sendAndExit('done');
            } 
        }
    },
    false, 
    false).
connect(uri);
