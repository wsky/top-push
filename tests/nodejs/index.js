var fork = require('child_process').fork,
    test_file = process.argv[2],
    test_total = parseInt(process.argv[3]);

var args = new Array(process.argv.length - 4);
for(var i = 0; i < args.length; i++){
    args[i] = process.argv[i + 4];
}
console.log(args);

for(var i = 0; i < test_total; i++) {
    var child = fork(test_file, args);
    child.on('message', function(m) {});
}

process.openStdin().addListener("data", function(d) { process.exit(); });