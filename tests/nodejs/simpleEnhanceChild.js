var count = 0,
    match = 0;

process.on('message', function(MSG, connection) {

    connection.on('close', function() {
        console.log('Connection Closed');
        process.exit();
    });
    connection.on('message', function(message) {
        if (message.type === 'utf8') {
            var msg = message.utf8Data;
            count++;
            if(msg == MSG) match++;
        }
    }); 
});
