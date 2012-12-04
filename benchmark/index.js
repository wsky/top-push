var fork = require('child_process').fork,
    test_count = 0,
    test_error_count = 0,
    test_file = process.argv[2],
    test_uri = process.argv[3],
    test_total = parseInt(process.argv[4]),
    test_message_count = parseInt(process.argv[5]),
    begin = null;

//console.log(process.argv);

function tryExit() {
    if(test_count >= test_total) {   
        var cost = new Date() - begin;
        console.log('\n-------------------\n');
        console.log('conections: %s, failures: %s, total messages: %s, cost: %sms, avg: %smsg/s', 
            test_count,
            test_error_count,
            test_count * test_message_count,
            cost,
            test_count * test_message_count * 1000 / cost);
        console.log('\n-------------------\n');
        process.exit();
    }
    else
        setTimeout(tryExit, 200);
}

console.log(process.argv);

begin = new Date();

for(var i = 0; i < test_total; i++) {
    var child = fork(test_file, [test_uri, test_message_count]);
    child.on('message', function(m) { 
        //console.log('stdout: ' + stdout);
        //console.log('stderr: ' + stderr);
        //if (error !== null) {
        //  console.log('exec error: ' + error);
        //}
        if(m == 'done')
            test_count++;
        else
            test_error_count++;
    });
}

tryExit();