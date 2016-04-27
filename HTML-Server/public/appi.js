/**
 * Created by utilisateur on 2016-02-23.
 */

;
jQuery(function($){
    'use strict';

    /**
     * All the code relevant to Socket.IO is collected in the IO namespace.
     *
     * @type {{init: Function, bindEvents: Function, onConnected: Function, onNewGameCreated: Function, playerJoinedRoom: Function, beginNewGame: Function, onNewWordData: Function, hostCheckAnswer: Function, gameOver: Function, error: Function}}
     */
    var IO = {

        /**
         * This is called when the page is displayed. It connects the Socket.IO client
         * to the Socket.IO server
         */
        init: function() {
            IO.socket = io.connect();
            IO.bindEvents();
        },

        /**
         * While connected, Socket.IO will listen to the following events emitted
         * by the Socket.IO server, then run the appropriate function.
         */
        bindEvents : function() {
            IO.socket.on('connected', IO.onConnected );
            IO.socket.on('newGameCreated', IO.onNewGameCreated );
            IO.socket.on('hostAsPlayerJoinedRoom', IO.playerJoinedRoom );
            IO.socket.on('playerJoinedRoom', IO.playerJoinedRoom );
            IO.socket.on('beginNewGame', IO.beginNewGame );
            IO.socket.on('hostCheckAction', IO.hostCheckAction);
            IO.socket.on('newTurn', IO.newTurn);
            IO.socket.on('newBoard', IO.newBoard);
            IO.socket.on('yourTurn', IO.yourTurn);
            IO.socket.on('gameOver', IO.gameOver);
            IO.socket.on('error', IO.error );
        },

        /**
         * The client is successfully connected!
         */
        onConnected : function() {
            // Cache a copy of the client's socket.IO session ID on the App
            App.mySocketId = IO.socket.socket.sessionid;
            // console.log(data.message);
        },

        /**
         * A new game has been created and a random game ID has been generated.
         * @param data {{ gameId: int, mySocketId: * }}
         */
        onNewGameCreated : function(data) {
            App.Host.gameInit(data);
        },

        /**
         * A player has successfully joined the game.
         * @param data {{playerName: string, gameId: int, mySocketId: int}}
         */
        playerJoinedRoom : function(data) {
            // When a player joins a room, do the updateWaitingScreen funciton.
            // There are two versions of this function: one for the 'host' and
            // another for the 'player'.
            //
            // So on the 'host' browser window, the App.Host.updateWiatingScreen function is called.
            // And on the player's browser, App.Player.updateWaitingScreen is called.
            App[App.myRole].updateWaitingScreen(data);
        },

        /**
         * Both players have joined the game.
         * @param data
         */
        beginNewGame : function(data) {
            App[App.myRole].gameCountdown(data);
        },

        newTurn : function() {
            if(App.myRole === 'Host') {
                App[App.myRole].newTurn();
            }
        },


        newBoard : function() {
            if(App.myRole === 'Player') {
                App[App.myRole].newBoard();
            }
        },

        yourTurn : function(data) {
            if(App.myRole === 'Player' && App[App.myRole].myName === data.playerName) {
                App[App.myRole].yourTurn();
            }
        },

        /**
         * A player made an action. If this is the host, check the action.
         * @param data
         */
        hostCheckAction : function(data) {
            if(App.myRole === 'Host') {
                App.Host.checkAction(data);
            }
        },

        /**
         * Let everyone know the game has ended.
         * @param data
         */
        gameOver : function(data) {
            App[App.myRole].endGame(data);
        },

        /**
         * An error has occurred.
         * @param data
         */
        error : function(data) {
            alert(data.message);
        }

    };

    var App = {

        /**
         * Keep track of the gameId, which is identical to the ID
         * of the Socket.IO Room used for the players and host to communicate
         *
         */
        gameId: 0,

        /**
         * This is used to differentiate between 'Host' and 'Player' browsers.
         */
        myRole: '',   // 'Player' or 'Host'

        /**
         * The Socket.IO socket object identifier. This is unique for
         * each player and host. It is generated when the browser initially
         * connects to the server when the page loads for the first time.
         */
        mySocketId: '',

        /**
         * Identifies the current round. Starts at 0 because it corresponds
         * to the array of word data stored on the server.
         */
        currentTurn: 0, //Player ID turn

        /* *************************************
         *                Setup                *
         * *********************************** */

        /**
         * This runs when the page initially loads.
         */
        init: function () {
            App.cacheElements();
            App.showInitScreen();
            App.bindEvents();

            // Initialize the fastclick library
            FastClick.attach(document.body);
        },

        /**
         * Create references to on-screen elements used throughout the game.
         */
        cacheElements: function () {
            App.$doc = $(document);

            // Templates
            App.$gameArea = $('#gameArea');
            App.$templateIntroScreen = $('#intro-screen-template').html();
            App.$templateInsGame = $('#instantiate-game-template').html();
            App.$templateNewGame = $('#create-game-template').html();
            App.$templateJoinGame = $('#join-game-template').html();
            App.$hostGame = $('#host-game-template').html();
        },

        /**
         * Create some click handlers for the various buttons that appear on-screen.
         */
        bindEvents: function () {
            // Host
            App.$doc.on('click', '#btnInstantiateGame', App.Host.onInstantiateClick);
            App.$doc.on('click', '#btnCreateGame', App.Host.onCreateClick);

            // Player
            App.$doc.on('click', '#btnJoinGame', App.Player.onJoinClick);
            App.$doc.on('click', '#btnStart',App.Player.onPlayerStartClick);
            App.$doc.on('click', '.btnAnswer',App.Player.onPlayerAnswerClick);
            App.$doc.on('click', '#btnPlayerRestart', App.Player.onPlayerRestart);
            App.$doc.on('click', '#btnBuyCage', App.Player.onPlayerBuyCage);
            App.$doc.on('click', '#btnBuyBooth', App.Player.onPlayerBuyBooth);
            App.$doc.on('click', '#btnBuyDino', App.Player.onPlayerBuyDino);
            App.$doc.on('click', '#btnBuy', App.Player.onPlayerBuy);
            App.$doc.on('click', '#btnAds', App.Player.onPlayerMakeAds);
        },

        /* *************************************
         *             Game Logic              *
         * *********************************** */

        /**
         * Show the initial Anagrammatix Title Screen
         * (with Start and Join buttons)
         */
        showInitScreen: function() {
            App.$gameArea.html(App.$templateIntroScreen);
            App.doTextFit('.title');
        },


        /* *******************************
         *         HOST CODE           *
         ******************************* */
        Host : {

            /**
             * Contains references to player data
             */
            players : [],

            /**
             * Flag to indicate if a new game is starting.
             * This is used after the first game ends, and players initiate a new game
             * without refreshing the browser windows.
             */
            isNewGame : false,

            /**
             * Keep track of the number of players that can joined the game.
             */
            numPlayersMax: 0,

            /**
             * Keep track of the number of players that have joined the game.
             */
            numPlayersInRoom: 0,

            /**
             * Keep track of the number of players that have played their turn.
             */
            numPlayersHasPlayed: 0,


            /**
             * Keep track of the number of turns the game have.
             */
            numTurnsMax: 0,

            /**
             * Keep track of the name of the Host.
             */
            hostName: '',


            /**
             * Handler for the "Create" button on the Title Screen.
             */
            onCreateClick: function () {
                // console.log('Clicked "Create A Game"');
                App.$gameArea.html(App.$templateInsGame);
            },


            /**
             * Handler for the "Instatiante" button on the Create Screen.
             */
            onInstantiateClick: function () {
                // console.log('Clicked "Create A Game"');

                // collect data to send to the server
                var data = {
                    numPlayersMax : +($('input[name="numberPlayers"]:checked').val()),
                    numTurnsMax : $('input[name="numberTurns"]:checked').val(),
                };

                App.Host.numPlayersMax = data.numPlayersMax;
                App.Host.numTurnsMax = data.numTurnsMax;
                App.Host.hostName = $('#inputHostName').val();
                //console.log(App.Host.numPlayersMax + ' ' + App.Host.numTurnsMax);
                IO.socket.emit('hostCreateNewGame', data);
            },


            /**
             * The Host screen is displayed for the first time.
             * @param data{{ gameId: int, mySocketId: * }}
             */
            gameInit: function (data) {
                App.gameId = data.gameId;
                App.mySocketId = data.mySocketId;
                App.myRole = 'Host';
                App.Host.numPlayersInRoom = 0;

                App.Host.displayNewGameScreen();
                // console.log("Game started with ID: " + App.gameId + ' by host: ' + App.mySocketId);
            },

            /**
             * Show the Host screen containing the game URL and unique game ID
             */
            displayNewGameScreen : function() {
                // Fill the game screen with the appropriate HTML
                App.$gameArea.html(App.$templateNewGame);

                // Display the URL on screen
                $('#gameURL').text(window.location.href);
                App.doTextFit('#gameURL');

                // Show the gameId / room id on screen
                $('#spanNewGameCode').text(App.gameId);
            },

            /**
             * Update the Host screen when the first player joins
             * @param data{{playerName: string}}
             */
            updateWaitingScreen: function(data) {
                // If this is a restarted game, show the screen.
                if ( App.Host.isNewGame ) {
                    App.Host.displayNewGameScreen();
                }
                // Update host screen
                $('#playersWaiting')
                    .append('<p/>')
                    .text('Player ' + data.playerName + ' joined the game.');

                // Store the new player's data on the Host.
                App.Host.players.push(data);

                // Increment the number of players in the room
                App.Host.numPlayersInRoom += 1;

                // If two players have joined, start the game!
                if (App.Host.numPlayersInRoom === App.Host.numPlayersMax) {
                    // console.log('Room is full. Almost ready!');

                    // Let the server know that two players are present.
                    IO.socket.emit('hostRoomFull',App.gameId);
                }
            },

            /**
             * Show the countdown screen
             */
            gameCountdown : function() {

                // Prepare the game screen with new HTML
                App.$gameArea.html(App.$hostGame);

                App.Host.loadBoard(App.Host.numPlayersMax);

                IO.socket.emit('hostCountdownFinished', {turnMax: App.Host.numTurnsMax, gameId: App.gameId});

                // Display the players' names on screen
                //console.log("Player1 name : " + App.Host.players[0].playerName + " " + App.Host.players[0].mySocketId);
                $('#player1Score')
                    .find('.playerName')
                    .html(App.Host.players[0].playerName);
                $('#player1Score').find('.cash').attr('id',App.Host.players[0].mySocketId);
                $('#player1Score').find('.visitors').attr('id',App.Host.players[0].mySocketId);
                $('#boards').find('.board1').attr('id',App.Host.players[0].mySocketId);

                if (App.Host.numPlayersMax >= 2) {
                    //console.log("Player2 name : " + App.Host.players[1].playerName + " " + App.Host.players[1].mySocketId);
                    $('#player2Score')
                        .find('.playerName')
                        .html(App.Host.players[1].playerName);
                    $('#player2Score').find('.cash').attr('id', App.Host.players[1].mySocketId);
                    $('#player2Score').find('.visitors').attr('id', App.Host.players[1].mySocketId);
                    $('#boards').find('.board2').attr('id',App.Host.players[1].mySocketId);
                }
                else {
                    $('.board2').remove();
                    $('#player2Score').remove();
                }

                if (App.Host.numPlayersMax >= 3) {
                    //console.log("Player3 name : " + App.Host.players[2].playerName +" " + App.Host.players[2].mySocketId);
                    $('#player3Score')
                        .find('.playerName')
                        .html(App.Host.players[2].playerName);
                    $('#player3Score').find('.cash').attr('id',App.Host.players[2].mySocketId);
                    $('#player3Score').find('.visitors').attr('id',App.Host.players[2].mySocketId);
                    $('#boards').find('.board3').attr('id',App.Host.players[2].mySocketId);
                }
                else {
                    $('.board3').remove();
                    $('#player3Score').remove();
                }

                if (App.Host.numPlayersMax >= 4) {
                    //console.log("Player4 name : " + App.Host.players[3].playerName + " " + App.Host.players[3].mySocketId);
                    $('#player4Score')
                        .find('.playerName')
                        .html(App.Host.players[3].playerName);
                    $('#player4Score').find('.cash').attr('id',App.Host.players[3].mySocketId);
                    $('#player4Score').find('.visitors').attr('id',App.Host.players[3].mySocketId);
                    $('#boards').find('.board4').attr('id',App.Host.players[3].mySocketId);
                }
                else {
                    $('.board4').remove();
                    $('#player4Score').remove();
                };
            },

            loadBoard : function(nbPlayers) {
                for (var i = 1; i <= nbPlayers; i++) {
                    var str = "<table class='board" + i + "'>";

                    switch (i) {
                        case 1:
                            str += ("<tr>" +
                            "<th>1</th> <th>2</th> <th>3</th> <th>4</th> <th>5</th> <th>6</th> <th>7</th> <th>8</th>" +
                            "<th>9</th> <th>10</th> <th>11</th> <th>12</th> <th>13</th> <th>14</th> <th>15</th>" +
                            "</tr>");

                            for (var y = 11; y >= 1; y--) {
                                str += ("<tr>");
                                for (var x = 1; x <= 15; x++) {
                                    if (x > 3 || y > 2) {
                                        str += ("<td class='tile' id='tile_" + x + "_" + y + "'></td>");
                                    } else {
                                        str += ("<td class='ntile' id='tile_" + x + "_" + y + "'></td>");
                                    }
                                }
                                str += ("<th>" + y + "</th>" +
                                "</tr>");
                            }
                            break;

                        case 2:
                            str += ("<tr>" +
                            "<th></th> <th>15</th> <th>14</th> <th>13</th> <th>12</th> <th>11</th> <th>10</th> <th>9</th>" +
                            "<th>8</th> <th>7</th> <th>6</th> <th>5</th> <th>4</th> <th>3</th> <th>2</th> <th>1</th>" +
                            "</tr>");

                            for (var y = 11; y >= 1; y--) {
                                str += ("<tr>" +
                                "<th>" + y + "</th>");
                                for (var x = 15; x >= 1; x--) {
                                    if (x > 3 || y > 2) {
                                        str += ("<td class='tile' id='tile_" + x + "_" + y + "'></td>");
                                    } else {
                                        str += ("<td class='ntile' id='tile_" + x + "_" + y + "'></td>");
                                    }
                                }
                                str += ("</tr>");
                            }
                            break;

                        case 3:
                            for (var y = 1; y <= 11; y++) {
                                str += ("<tr>" +
                                "<th>" + y + "</th>");
                                for (var x = 15; x >= 1; x--) {
                                    if (x > 3 || y > 2) {
                                        str += ("<td class='tile' id='tile_" + x + "_" + y + "'></td>");
                                    } else {
                                        str += ("<td class='ntile' id='tile_" + x + "_" + y + "'></td>");
                                    }
                                }
                                str += ("</tr>");
                            }

                            str += ("<tr>" +
                            "<th></th> <th>15</th> <th>14</th> <th>13</th> <th>12</th> <th>11</th> <th>10</th> <th>9</th>" +
                            "<th>8</th> <th>7</th> <th>6</th> <th>5</th> <th>4</th> <th>3</th> <th>2</th> <th>1</th>" +
                            "</tr>");
                            break;

                        case 4:

                            for (var y = 1; y <= 11; y++) {
                                str += ("<tr>");
                                for (var x = 1; x <= 15; x++) {
                                    if (x > 3 || y > 2) {
                                        str += ("<td class='tile' id='tile_" + x + "_" + y + "'></td>");
                                    } else {
                                        str += ("<td class='ntile' id='tile_" + x + "_" + y + "'></td>");
                                    }
                                }
                                str += ("<th>" + y + "</th>" +
                                "</tr>");
                            }

                            str += ("<tr>" +
                            "<th>1</th> <th>2</th> <th>3</th> <th>4</th> <th>5</th> <th>6</th> <th>7</th> <th>8</th>" +
                            "<th>9</th> <th>10</th> <th>11</th> <th>12</th> <th>13</th> <th>14</th> <th>15</th>" +
                            "</tr>");
                            break;
                    }
                    $('#boards').append(str);
                }
            },

            newTurn : function() {
                App.Host.numPlayersHasPlayed = 0;

                var data = {
                    playerName:  App.Host.players[App.Host.numPlayersHasPlayed].playerName,
                    turnPlayed:  App.currentTurn
                }

                //console.log('Player ' + data.playerName + 'turn');
                IO.socket.emit('playerTurn', data);
            },

            /**
             * Check the answer clicked by a player.
             * @param data{{round: *, playerId: *, action: {act:*, type:*, coord:[x,y]}, gameId: *}}
             */
            checkAction : function(data) {
                // Verify that the answer clicked is from the current round.
                // This prevents a 'late entry' from a player whos screen has not
                // yet updated to the current round.

                // Get the player's score
                var $pCash = $('.cash').filter('#' + data.playerId);
                var $pVisitors = $('.visitors').filter('#' + data.playerId);
                var $pBoard = $('#boards').children('#' + data.playerId);
                //console.log(data + $pCash + $pVisitors);

                // Update Visitors
                //console.log("Before Phase" + $pVisitors.text());

                switch (data.action) {
                    // Advance player's score if it is correct
                    case 'playerBuyCage':
                    {
                        // Add 5 to the player's score
                        $pCash.text(+$pCash.text() - 5);

                        var xB = parseInt(data.coordX)+1; var yB = parseInt(data.coordY)+1;
                        for (var x=data.coordX; x <= xB; x++ ) {
                            for(var y=data.coordY; y <= yB; y++ ) {
                                var tile = '#tile_'+x+'_'+y;
                                $pBoard.find(tile).attr('class','cage');
                            }
                        }
                        // Set player board on host and player page
                    } break;
                    case 'playerBuyDino':
                    {
                        switch (data.type) {
                            case '3':
                            {
                                // Add 2 visitors and Sub 5 cash to the player's score
                                $pCash.text(+$pCash.text() - 5);
                                //$pVisitors.text(+$pVisitors.text() + 2);

                                // Set player board on host and player page
                            } break;
                            case '4':
                            {
                                // Add 1 visitor and Sub 2 cash to the player's score
                                $pCash.text(+$pCash.text() - 2);
                                //$pVisitors.text(+$pVisitors.text() + 1);

                                // Set player board on host and player page
                            } break;
                            case '5':
                            {
                                // Add 1 visitor and Sub 2 cash to the player's score
                                $pCash.text(+$pCash.text() - 10);
                                //$pVisitors.text(+$pVisitors.text() + 5);

                                // Set player board on host and player page
                            } break;
                            case '2':
                            {
                                // Add 1 visitor and Sub 2 cash to the player's score
                                $pCash.text(+$pCash.text() - 20);
                                //$pVisitors.text(+$pVisitors.text() + 10);

                                // Set player board on host and player page
                            } break;
                        }

                        var tile = '#tile_'+data.coordX+'_'+data.coordY;
                        $pBoard.find(tile).attr('class','dino');

                    } break;

                    case 'playerBuyBooth':
                    {
                        $pCash.text(+$pCash.text() - 3);
                        //switch(data.action.type) {}
                        var tile = '#tile_'+data.coordX+'_'+data.coordY;
                        $pBoard.find(tile).attr('class','booth');

                    } break;

                    case 'playerMakeAds':
                    {
                        $pCash.text(+$pCash.text() + (+$pVisitors.text()));
                    } break;

                }

                // Income Phase
                $pVisitors.text(data.visitors);
                //console.log("Income Phase : " + data.visitors);
                $pCash.text(+$pCash.text() + ( data.visitors*2));

                // Advance the round
                App.Host.numPlayersHasPlayed += 1;


                if(App.Host.numPlayersHasPlayed === App.Host.numPlayersMax) {

                    App.currentTurn += 1;

                    // Prepare data to send to the server
                    var data = {
                        gameId: App.gameId,
                        turn: App.currentTurn,
                        maxTurn: App.Host.numTurnsMax
                    }

                    // Notify the server to start the next round.
                    IO.socket.emit('hostNextTurn', data);
                }
                else {
                    App.Host.nextPlayer();
                }
               // }
            },

            nextPlayer: function() {

                var data = {
                    playerName:  App.Host.players[App.Host.numPlayersHasPlayed].playerName
                }

                //console.log('Player ' + data.playerName + 'turn');
                IO.socket.emit('playerTurn', data);

                //console.log(App.Host.numPlayersHasPlayed + ' === ' + App.Host.numPlayersMax);
            },

            /**
             * All 10 rounds have played out. End the game.
             * @param data
             */
            endGame : function(data) {

                var $playersScores = [];

                // Get the data for player 1 from the host screen
                var $p1 = $('#player1Score');
                var p1Score = +$p1.find('.visitors').text();
                var p1Name = $p1.find('.playerName').text();
                $playersScores.push({score: p1Score, name: p1Name});

                if (App.Host.numPlayersMax >= 2) {
                    // Get the data for player 2 from the host screen
                    var $p2 = $('#player2Score');
                    var p2Score = +$p2.find('.visitors').text();
                    var p2Name = $p2.find('.playerName').text();
                    $playersScores.push({score: p2Score, name: p2Name});
                }

                if (App.Host.numPlayersMax >= 3) {
                    // Get the data for player 3 from the host screen
                    var $p3 = $('#player3Score');
                    var p3Score = +$p3.find('.visitors').text();
                    var p3Name = $p3.find('.playerName').text();
                    $playersScores.push({score: p3Score, name: p3Name});
                }

                if (App.Host.numPlayersMax >= 4) {
                    // Get the data for player 3 from the host screen
                    var $p4 = $('#player4Score');
                    var p4Score = +$p4.find('.visitors').text();
                    var p4Name = $p4.find('.playerName').text();
                    $playersScores.push({score: p4Score, name: p4Name});
                }

                // Find the winner based on the scores
                var winner, tie=false, tampon = 0;

                $.each( $playersScores, function( i, player ) {
                    if(player.score > tampon) {
                        winner = player;
                        tampon = player.score;
                    }
                });

                // Display the winner (or tie game message)
                if(tie){
                    $('#hostWord').text("It's a Tie!");
                } else {
                    $('#hostWord').text( winner.name + ' Wins!!' );
                }
                App.doTextFit('#hostWord');

                // Reset game data
                App.Host.numPlayersInRoom = 0;
                App.Host.isNewGame = true;
            },

            /**
             * A player hit the 'Start Again' button after the end of a game.
             */
            restartGame : function() {
                App.$gameArea.html(App.$templateNewGame);
                App.currentRound = 0;
                App.Host.numPlayersHasPlayed =0;
                $('#spanNewGameCode').text(App.gameId);
            }
        },


        /* *****************************
         *        PLAYER CODE        *
         ***************************** */

        Player : {

            /**
             * A reference to the socket ID of the Host
             */
            hostSocketId: '',

            /**
             * The player's name entered on the 'Join' screen.
             */
            myName: '',

            cash: 15,

            visitors: 0,

            cage: [],

            dinos: {Velociraptor:[], Brontosaurus:[], Triceratops:[], Tyrannosaurus:[]},

            booths: {Restaurant:[], Security:[], Bathroom:[], Casino:[], Spy:[], Paleontologist:[]},

            /**
             * Click handler for the 'JOIN' button
             */
            onJoinClick: function () {
                // console.log('Clicked "Join A Game"');

                // Display the Join Game HTML on the player's screen.
                App.$gameArea.html(App.$templateJoinGame);
            },

            /**
             * The player entered their name and gameId (hopefully)
             * and clicked Start.
             */
            onPlayerStartClick: function() {
                // console.log('Player clicked "Start"');

                // collect data to send to the server
                var data = {
                    gameId : +($('#inputGameId').val()),
                    playerName : $('#inputPlayerName').val() || 'anon'
                };

                // Send the gameId and playerName to the server
                IO.socket.emit('playerJoinGame', data);

                // Set the appropriate properties for the current player.
                App.myRole = 'Player';
                App.Player.myName = data.playerName;
            },

            /**
             *  Click handler for the "Start Again" button that appears
             *  when a game is over.
             */
            onPlayerRestart : function() {
                var data = {
                    gameId : App.gameId,
                    playerName : App.Player.myName
                }
                IO.socket.emit('playerRestart',data);
                App.currentRound = 0;
                App.Player.cash= 15;
                App.Player.visitors= 0;
                $('#gameArea').html("<h3>Waiting on host to start new game.</h3>");
            },

            /**
             *  Click handler for the "Cage" button that appears
             *  when its player Turn
             */
            onPlayerBuyCage : function() {
                $('#inputBuy').remove();
                $('#gameArea').append($("<div id='inputBuy'>" +
                    "<div>" +
                    "<label for='inputCoordX'>Coord X :</label>" +
                    "<input id='inputCoordX' type='text' />" +
                    "</div>" +
                    "<div>" +
                    "<label for='inputCoordY'>Coord Y :</label>" +
                    "<input id='inputCoordY' type='text' />" +
                    "</div>" +
                    "<button id='btnBuy' value='cage'>BUY</button>" +
                    "</div>"));
            },

            /**
             *  Click handler for the "Dino" button that appears
             *  when its player Turn
             */
            onPlayerBuyDino : function() {
                $('#inputBuy').remove();
                $('#gameArea').append($("<div id='inputBuy'>" +
                    "<div>" +
                    "<label for='inputCoordX'>Coord X :</label>" +
                    "<input id='inputCoordX' type='text' />" +
                    "</div>" +
                    "<div>" +
                    "<label for='inputCoordY'>Coord Y :</label>" +
                    "<input id='inputCoordY' type='text' />" +
                    "</div>" +
                    "<label id='inputdinoType'>Type of Dinosaur :</label>" +
                    "<input type='radio' id='inputVelo' name='dinoType' value=3 checked> Velociraptor" +
                    "<input type='radio' id='inputBront' name='dinoType' value=4> Brontosaurus" +
                    "<input type='radio' id='inputTric' name='dinoType' value=5> Triceratops" +
                    "<input type='radio' id='inputTyra' name='dinoType' value=2> Tyrannosaurus" +
                    "<button id='btnBuy' value='dino'>BUY</button>" +
                    "</div>"));
            },

            /**
             *  Click handler for the "Booth" button that appears
             *  when its player Turn
             */
            onPlayerBuyBooth : function() {
                $('#inputBuy').remove();
                $('#gameArea').append($("<div id='inputBuy'>" +
                    "<div>" +
                    "<label for='inputCoordX'>Coord X :</label>" +
                    "<input id='inputCoordX' type='text' />" +
                    "</div>" +
                    "<div>" +
                    "<label for='inputCoordY'>Coord Y :</label>" +
                    "<input id='inputCoordY' type='text' />" +
                    "</div>" +
                    "<label id='inputboothType'>Type of Dinosaur :</label>" +
                    "<input type='radio' id='inputRest' name='boothType' value=6 checked> Restaurant" +
                    "<input type='radio' id='inputSecu' name='boothType' value=7> Security" +
                    "<input type='radio' id='inputBath' name='boothType' value=8> Bathroom" +
                    "<input type='radio' id='inputCasi' name='boothType' value=9> Casino" +
                    "<input type='radio' id='inputSpy' name='boothType' value=10> Spy" +
                    "<input type='radio' id='inputPale' name='boothType' value=11> Paleontologist" +
                    "<button id='btnBuy' value='booth'>BUY</button>" +
                    "</div>"));
            },

            onPlayerBuy : function() {
                var $btnValue = $('#btnBuy').val();
                App.Player.turnPlayed += 1;

                switch($btnValue) {
                    case 'cage':
                        var data = {
                            gameId : App.gameId,
                            playerId : App.mySocketId,
                            playerName : App.Player.myName,
                            action:'playerBuyCage',
                            coordX : $('#inputCoordX').val(),
                            coordY : $('#inputCoordY').val(),
                            visitors : App.Player.visitors
                        }
                        App.Player.incomeVisitors();
                        data.visitors = App.Player.visitors;
                        IO.socket.emit('playerAction',data);

                        var xB = parseInt(data.coordX)+1; var yB = parseInt(data.coordY)+1;
                        var tileInfo = [];
                        for (var x=data.coordX; x <= xB; x++ ) {
                            for(var y=data.coordY; y <= yB; y++ ) {
                                var tile = '#tile_'+x+'_'+y;
                                $(tile).attr('class','cage');
                                tileInfo.push(tile);
                            }
                        }
                        App.Player.cage.push(tileInfo);

                        $('#gameArea').append($("<h3>Player " + data.playerName + "has bought a cage. He's got " + App.Player.cage.length + "</h3>"));
                        break;

                    case 'dino':
                        var data = {
                            gameId : App.gameId,
                            playerId : App.mySocketId,
                            playerName : App.Player.myName,
                            action:'playerBuyDino',
                            coordX : $('#inputCoordX').val(),
                            coordY : $('#inputCoordY').val(),
                            type : $('input[name="dinoType"]:checked').val(),
                            visitors : App.Player.visitors
                        }

                        var type;
                        switch (parseInt(data.type)) {
                            case 3: type = "Velociraptor";  break;
                            case 4: type = "Brontosaurus";  break;
                            case 5: type = "Triceratops";   break;
                            case 2: type = "Tyrannosaurus"; break;
                        }

                        var tile = '#tile_'+data.coordX+'_'+data.coordY;
                        $(tile).attr('class','dino');
                        App.Player.dinos[type].push(tile);

                        App.Player.incomeVisitors();
                        data.visitors = App.Player.visitors;
                        IO.socket.emit('playerAction',data);

                        $('#gameArea').append($("<h3>Player " + data.playerName + "has bought a " + type + " . He's got " + App.Player.dinos[type].length + "</h3>"));
                        break;

                    case 'booth':
                        var data = {
                            gameId : App.gameId,
                            playerId : App.mySocketId,
                            playerName : App.Player.myName,
                            action:'playerBuyBooth',
                            coordX : $('#inputCoordX').val(),
                            coordY : $('#inputCoordY').val(),
                            type : $('input[name="boothType"]:checked').val(),
                            visitors : App.Player.visitors
                        }
                        App.Player.incomeVisitors();
                        data.visitors = App.Player.visitors;
                        IO.socket.emit('playerAction',data);

                        var type;
                        switch(parseInt(data.type)) {
                            case 6: type = "Restaurant"; break;
                            case 7: type = "Security"; break;
                            case 8: type = "Bathroom"; break;
                            case 9: type = "Casino"; break;
                            case 10: type = "Spy"; break;
                            case 11: type = "Paleontologist"; break;
                        }
                        var tile = '#tile_'+data.coordX+'_'+data.coordY;
                        $(tile).attr('class','booth');
                        App.Player.booths[type].push(tile);

                        $('#gameArea').append($("<h3>Player " + data.playerName + "has bought a " + type + " . He's got " + App.Player.booths[type].length + "</h3>"));
                        break;
                }
                $('#inputBuy').remove();
                $('#playerOption').remove();
            },

            onPlayerMakeAds : function() {
                $('#inputBuy').remove();
                $('#playerOption').remove();

                App.Player.turnPlayed += 1;

                var data = {
                    gameId: App.gameId,
                    playerId : App.mySocketId,
                    playerName: App.Player.myName,
                    action:'playerMakeAds',
                    visitors : App.Player.visitors
                }
                App.Player.incomeVisitors();
                data.visitors = App.Player.visitors;
                IO.socket.emit('playerAction',data);

                $('#gameArea').append("<h3>Player " + data.playerName + " has made an ad </h3>");
            },

            /**
             * Display the waiting screen for player 1
             * @param data
             */
            updateWaitingScreen : function(data) {
                if(IO.socket.socket.sessionid === data.mySocketId){
                    App.myRole = 'Player';
                    App.gameId = data.gameId;

                    $('#playerWaitingMessage')
                        .append('<p/>')
                        .text('Joined Game ' + data.gameId + '. Please wait for game to begin.');
                }
            },

            /**
             * Display 'Get Ready' while the countdown timer ticks down.
             * @param hostData
             */
            gameCountdown : function(hostData) {
                App.Player.hostSocketId = hostData.mySocketId;
                $('#gameArea')
                    .html('<div class="gameOver">Time to play!</div>');
            },

            /**
             * Show the board of the player
             * @param data{}
             */
            newBoard : function() {
                // Create an board hard coded
                var str = "<table id='board'>";

                str += ("<tr>" +
                "<th>1</th> <th>2</th> <th>3</th> <th>4</th> <th>5</th> <th>6</th> <th>7</th> <th>8</th>" +
                "<th>9</th> <th>10</th> <th>11</th> <th>12</th> <th>13</th> <th>14</th> <th>15</th>" +
                "</tr>");

                for(var y = 11; y >= 1; y--) {
                    str += ("<tr>");
                    for (var x = 1; x <= 15; x++) {
                        if (x > 3 || y > 2) {
                            str += ("<td class='tile' id='tile_" + x + "_" + y + "'></td>");
                        } else {
                            str += ("<td class='ntile' id='tile_" + x + "_" + y + "'></td>");
                        }
                    }
                    str += ("<th>" + y + "</th>" +
                    "</tr>");
                }

                // Insert the list onto the screen.
                $('#gameArea').html(str);
            },

            yourTurn: function() {
               // $('#playerOption').remove();

                var $str = $("<div id='playerOption'>" +
                    "<button id='btnBuyCage'>CAGE</button>" +
                    "<button id='btnBuyBooth'>BOOTH</button>" +
                    "<button id='btnBuyDino'>DINO</button>" +
                    "<button id='btnAds'>ADS</button>" +
                    "</div>");

                $('#gameArea').append($str);
            },

            incomeVisitors: function() {
                var dinoVisitors = 2;
                for(var i=0; i < App.Player.dinos.Velociraptor.length; i++){
                    App.Player.visitors += dinoVisitors;
                }

                dinoVisitors = 1;
                for(var i=0; i < App.Player.dinos.Brontosaurus.length; i++){
                    App.Player.visitors += dinoVisitors;
                }

                dinoVisitors = 5;
                for(var i=0; i < App.Player.dinos.Triceratops.length; i++){
                    App.Player.visitors += dinoVisitors;
                }

                dinoVisitors = 10;
                for(var i=0; i < App.Player.dinos.Tyrannosaurus.length; i++){
                    App.Player.visitors += dinoVisitors;
                }
            },
            /**
             * Show the "Game Over" screen.
             */
            endGame : function() {
                $('#gameArea')
                    .html('<div class="gameOver">Game Over!</div>')
                    .append(
                        // Create a button to start a new game.
                        $('<button>Start Again</button>')
                            .attr('id','btnPlayerRestart')
                            .addClass('btn')
                            .addClass('btnGameOver')
                    );
            }
        },


        /* **************************
         UTILITY CODE
         ************************** */

        /**
         * Make the text inside the given element as big as possible
         * See: https://github.com/STRML/textFit
         *
         * @param el The parent element of some text
         */
        doTextFit : function(el) {
            textFit(
                $(el)[0],
                {
                    alignHoriz:true,
                    alignVert:false,
                    widthOnly:true,
                    reProcess:true,
                    maxFontSize:300
                }
            );
        }

    };

    IO.init();
    App.init();

}($));


