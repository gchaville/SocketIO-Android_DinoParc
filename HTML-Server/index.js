/**
 * Created by utilisateur on 2016-02-23.
 */
// Import the Express module
var express = require('express');

// Import the 'path' module (packaged with Node.js)
var path = require('path');

// Create a new instance of Express
var app = express();

// Import the Anagrammatix game file.
var dnp = require('./dino');

// Create a simple Express application
// Turn down the logging activity
app.use(express.logger('dev'));

// Serve static html, js, css, and image files from the 'public' directory
app.use(express.static(path.join(__dirname,'public')));

var port = process.env.PORT || 8080;

// Create a Node.js based http server on port 80
var server = require('http').createServer(app).listen(port, function() {
    console.log('Listening to port:  %d', port);
});

// Create a Socket.IO server and attach it to the http server
var io = require('socket.io').listen(server);

// Reduce the logging output of Socket.IO
io.set('log level',1);

// Listen for Socket.IO Connections. Once connected, start the game logic.
io.sockets.on('connection', function (socket) {
    console.log('client connected');
    dnp.initGame(io, socket);
});


