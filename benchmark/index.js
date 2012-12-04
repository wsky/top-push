var exec = require('child_process').exec,
    test_count = 0,
    test_total = parseInt(process.argv[2]),
    begin = null;

function tryExit() {
    if(test_count >= test_total) {   
        var cost = new Date() - begin;
        console.log('count: %s, cost: %s ms, avg: %s msg/s', 
            test_count, 
            cost,
            test_count * 10 * 10000 * 1000 / cost);
        process.exit();
    }
    else
        setTimeout(tryExit, 200);
}

console.log(process.argv);

for(var i = 0; i < test_total; i++)
    exec('node receiveTest.js', function(error, stdout, stderr) { 
        console.log('stdout: ' + stdout);
        console.log('stderr: ' + stderr);
        if (error !== null) {
          console.log('exec error: ' + error);
        }
        test_count++;
    });

begin = new Date();
tryExit();