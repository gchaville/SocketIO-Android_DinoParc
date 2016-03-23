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
            IO.socket.on('nextTurn', IO.newTurn);
            IO.socket.on('newBoard', IO.newBoard);
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

        newTurn : function(data) {
            App.currentTurn = data.turn;

        },

        newBoard : function() {
            if(App.myRole === 'Player') {
                App[App.myRole].newBoard();
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
                console.log(App.Host.numPlayersMax + ' ' + App.Host.numTurnsMax);
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
                App.doTextFit('#hostWord');

                // Begin the on-screen countdown timer
                var $secondsLeft = $('#hostWord');
                App.countDown($secondsLeft, 5, function () {
                    IO.socket.emit('hostCountdownFinished', App.gameId);
                });

                // Display the players' names on screen
                console.log("Player1 name : " + App.Host.players[0].playerName + " " + App.Host.players[0].mySocketId);
                $('#player1Score')
                    .find('.playerName')
                    .html(App.Host.players[0].playerName);
                $('#player1Score').find('.cash').attr('id',App.Host.players[0].mySocketId);
                $('#player1Score').find('.visitors').attr('id',App.Host.players[0].mySocketId);

                console.log("Player2 name : " + App.Host.players[1].playerName + " " +  App.Host.players[1].mySocketId );
                $('#player2Score')
                    .find('.playerName')
                    .html(App.Host.players[1].playerName);
                $('#player2Score').find('.cash').attr('id',App.Host.players[1].mySocketId);
                $('#player2Score').find('.visitors').attr('id',App.Host.players[1].mySocketId);

                if (App.Host.numPlayersMax == 3) {
                    console.log("Player3 name : " + App.Host.players[2].playerName +" " + App.Host.players[2].mySocketId);
                    $('#player3Score')
                        .find('.playerName')
                        .html(App.Host.players[2].playerName);
                    $('#player3Score').find('.cash').attr('id',App.Host.players[2].mySocketId);
                    $('#player3Score').find('.visitors').attr('id',App.Host.players[2].mySocketId);
                }

                if (App.Host.numPlayersMax == 4) {
                    console.log("Player4 name : " + App.Host.players[3].playerName + " " + App.Host.players[3].mySocketId);
                    $('#player4Score')
                        .find('.playerName')
                        .html(App.Host.players[3].playerName);
                    $('#player4Score').find('.cash').attr('id',App.Host.players[3].mySocketId);
                    $('#player4Score').find('.visitors').attr('id',App.Host.players[3].mySocketId);
                }
            },

            /**
             * Check the answer clicked by a player.
             * @param data{{round: *, playerId: *, action: {act:*, type:*, coord:[x,y]}, gameId: *}}
             */
            checkAction : function(data) {
                // Verify that the answer clicked is from the current round.
                // This prevents a 'late entry' from a player whos screen has not
                // yet updated to the current round.
                if (data.round === App.currentRound){

                    // Get the player's score
                    var $pCash = $('.cash').filter('#' + data.playerId);
                    var $pVisitors = $('.visitors').filter('#' + data.playerId);

                    switch (data.action.act) {
                        // Advance player's score if it is correct
                        case 'cage':
                        {
                            // Add 5 to the player's score
                            $pCash.text(+$pCash.text() - 5);

                            // Set player board on host and player page
                        } break;
                        case 'dino':
                        {
                            switch (data.action.type) {
                                case 'velo':
                                {
                                    // Add 2 visitors and Sub 5 cash to the player's score
                                    $pCash.text(+$pCash.text() - 5);
                                    $pVisitors.text(+$pVisitors.text() + 2);

                                    // Set player board on host and player page
                                } break;
                                case 'bront':
                                {
                                    // Add 1 visitor and Sub 2 cash to the player's score
                                    $pCash.text(+$pCash.text() - 2);
                                    $pVisitors.text(+$pVisitors.text() + 1);

                                    // Set player board on host and player page
                                } break;
                                case 'tric':
                                {
                                    // Add 1 visitor and Sub 2 cash to the player's score
                                    $pCash.text(+$pCash.text() - 10);
                                    $pVisitors.text(+$pVisitors.text() + 5);

                                    // Set player board on host and player page
                                } break;
                                case 'tyra':
                                {
                                    // Add 1 visitor and Sub 2 cash to the player's score
                                    $pCash.text(+$pCash.text() - 20);
                                    $pVisitors.text(+$pVisitors.text() + 10);

                                    // Set player board on host and player page
                                } break;
                            }

                        } break;

                        case 'booth':
                        {
                            $pCash.text(+$pCash.text() - 3);
                            //switch(data.action.type) {}
                            // Set player board on host and player page
                        }

                    }
                    // Advance the round
                    App.currentRound += 1;

                    // Prepare data to send to the server
                    var data = {
                        gameId: App.gameId,
                        round: App.currentRound
                    }

                    // Notify the server to start the next round.
                    IO.socket.emit('hostNextTurn', data);
                }
            },

            /**
             * All 10 rounds have played out. End the game.
             * @param data
             */
            endGame : function(data) {
                // Get the data for player 1 from the host screen
                var $p1 = $('#player1Score');
                var p1Score = +$p1.find('.score').text();
                var p1Name = $p1.find('.playerName').text();

                // Get the data for player 2 from the host screen
                var $p2 = $('#player2Score');
                var p2Score = +$p2.find('.score').text();
                var p2Name = $p2.find('.playerName').text();

                // Find the winner based on the scores
                var winner = (p1Score < p2Score) ? p2Name : p1Name;
                var tie = (p1Score === p2Score);

                // Display the winner (or tie game message)
                if(tie){
                    $('#hostWord').text("It's a Tie!");
                } else {
                    $('#hostWord').text( winner + ' Wins!!' );
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

            cash: 10,

            visitors: 0,

            cage: {},

            dinos: {Velociraptor:[], Brontosaurus:[], Triceratop:[], Tyrannosaurus:[]},

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
                $('#gameArea').html("<h3>Waiting on host to start new game.</h3>");
            },

            /**
             *  Click handler for the "Cage" button that appears
             *  when its player Turn
             */
            onPlayerBuyCage : function() {
                $('#gameArea').append($("<div>" +
                    "<label for='inputCoordX'>Coord X :</label>" +
                    "<input id='inputCoordX' type='text' />" +
                    "</div>"))
                $('#gameArea').append($("<div>" +
                    "<label for='inputCoordY'>Coord X :</label>" +
                    "<input id='inputCoordY' type='text' />" +
                    "</div>"))
                $('#gameArea').append($("<button id='btnBuy' value='cage'>BUY</button>"));
            },

            /**
             *  Click handler for the "Dino" button that appears
             *  when its player Turn
             */
            onPlayerBuyDino : function() {
                $('#gameArea').append($("<div>" +
                    "<label for='inputCoordX'>Coord X :</label>" +
                    "<input id='inputCoordX' type='text' />" +
                    "</div>"))
                $('#gameArea').append($("<div>" +
                    "<label for='inputCoordY'>Coord X :</label>" +
                    "<input id='inputCoordY' type='text' />" +
                    "</div>"))
                $('#gameArea').append($("<label id='inputdinoType'>Type of Dinosaur :</label>" +
                    "<input type='radio' id='inputVelo' name='dinoType' value='Velociraptor' checked> Velociraptor" +
                    "<input type='radio' id='inputBront' name='dinoType' value='Brontosaurus'> Brontosaurus" +
                    "<input type='radio' id='inputTric' name='dinoType' value='Triceratop'> Triceratop" +
                    "<input type='radio' id='inputTyra' name='dinoType' value='Tyrannosaurus'> Tyrannosaurus"));
                $('#gameArea').append($("<button id='btnBuy' value='dino'>BUY</button>"));
            },

            /**
             *  Click handler for the "Booth" button that appears
             *  when its player Turn
             */
            onPlayerBuyBooth : function() {
                $('#gameArea').append($("<div>" +
                    "<label for='inputCoordX'>Coord X :</label>" +
                    "<input id='inputCoordX' type='text' />" +
                    "</div>"));
                $('#gameArea').append($("<div>" +
                    "<label for='inputCoordY'>Coord X :</label>" +
                    "<input id='inputCoordY' type='text' />" +
                    "</div>"));
                $('#gameArea').append($("<label id='inputboothType'>Type of Dinosaur :</label>" +
                    "<input type='radio' id='inputRest' name='boothType' value='Restaurant' checked> Restaurant" +
                    "<input type='radio' id='inputSecu' name='boothType' value='Security'> Security" +
                    "<input type='radio' id='inputBath' name='boothType' value='Bathroom'> Bathroom" +
                    "<input type='radio' id='inputCasi' name='boothType' value='Casino'> Casino" +
                    "<input type='radio' id='inputSpy' name='boothType' value='Spy'> Spy" +
                    "<input type='radio' id='inputPale' name='boothType' value='Paleontologist'> Paleontologist"));
                $('#gameArea').append($("<button id='btnBuy' value='booth'>BUY</button>"));
            },

            onPlayerBuy : function() {
                var $btnValue = $('#btnBuy').val();

                switch($btnValue) {
                    case 'cage':
                        var data = {
                            gameId : App.gameId,
                            playerName : App.Player.myName,
                            action:'playerBuyCage',
                            coordX : $('#inputCoordX').val(),
                            coordY : $('#inputCoordY').val()
                        }
                        IO.socket.emit('playerAction',data);

                        var $cage = [App.Player.cage.length]
                        for (var x=data.coordX; x <= data.coordX+1; x++ ) {
                            for(var y=data.coordY; y <= data.coordY+1; y++ ) {
                                var $tile = '#tile_'+data.coordX+'_'+data.coordY;
                                $($tile).attr('class','cage');
                                App.Player.cage.push($tile);
                            }
                        }

                        $('#gameArea').html("<h3>Player " + data.playerName + "has bought a cage. He's got " + App.Player.cage.length + "</h3>");
                        break;
                    case 'dino':
                        var data = {
                            gameId : App.gameId,
                            playerName : App.Player.myName,
                            action:'playerBuyDino',
                            coordX : $('#inputCoordX').val(),
                            coordY : $('#inputCoordY').val(),
                            dinoType : $('input[name="dinoType"]:checked').val(),
                        }
                        IO.socket.emit('playerAction',data);

                        var $tile = '#tile_'+data.coordX+'_'+data.coordY;
                        $($tile).attr('class','dino');
                        App.Player.dinos.hasOwnProperty(data.dinoType).push($tile);

                        $('#gameArea').html("<h3>Player " + data.playerName + "has bought a " + data.dinoType + " . He's got " + App.Player.dinos.hasOwnProperty(data.dinoType).length + "</h3>");
                        break;
                    case 'booth':
                        var data = {
                            gameId : App.gameId,
                            playerName : App.Player.myName,
                            action:'playerBuyBooth',
                            coordX : $('#inputCoordX').val(),
                            coordY : $('#inputCoordY').val(),
                            boothType : $('input[name="boothType"]:checked').val(),
                        }
                        IO.socket.emit('playerAction',data);

                        var $tile = '#tile_'+data.coordX+'_'+data.coordY;
                        $($tile).attr('class','booth');
                        App.Player.booths.hasOwnProperty(data.boothType).push($tile);

                        $('#gameArea').html("<h3>Player " + data.playerName + "has bought a " + data.boothType + " . He's got " + App.Player.booths.hasOwnProperty(data.boothType).length + "</h3>");
                        break;
                }
            },

            onPlayerMakeAds : function() {
                var data = {
                    gameId: App.gameId,
                    playerName: App.Player.myName,
                    action:'playerMakeAds'
                }
                IO.socket.emit('playerAction',data);

                $('#gameArea').html("<h3>Player " + data.playerName + "has bought a " + data.boothType + " . He's got " + App.Player.booths.hasOwnProperty(data.boothType).length + "</h3>");
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
                    .html('<div class="gameOver">Get Ready!</div>');
            },

            /**
             * Show the board of the player
             * @param data{}
             */
            newBoard : function() {
                // Create an board hard coded
                var $str = $(
                    "<table id='board'>" +
                    "<tr>" +
                    "<th>1</th> <th>2</th> <th>3</th> <th>4</th> <th>5</th> <th>6</th> <th>7</th>" +
                    "<th>8</th> <th>9</th> <th>10</th> <th>11</th> <th>12</th> <th>13</th> <th>14</th>" +
                    "</tr>" +
                    "<tr>" +
                    "<td class='tile' id='tile_10_1'></td> <td class='tile' id='tile_10_2'></td> <td class='tile' id='tile_10_3'></td> <td class='tile' id='tile_10_4'></td>" +
                    "<td class='tile' id='tile_10_5'></td> <td class='tile' id='tile_10_6'></td> <td class='tile' id='tile_10_7'></td> <td class='tile' id='tile_10_8'></td>" +
                    "<td class='tile' id='tile_10_9'></td> <td class='tile' id='tile_10_10'></td> <td class='tile' id='tile_10_11'></td> <td class='tile' id='tile_10_12'></td>" +
                    "<td class='tile' id='tile_10_13'></td> <td class='tile' id='tile_10_14'></td> <th>10</th>" +
                    "</tr>" +
                    "<tr>" +
                    "<td class='tile' id='tile_9_1'></td> <td class='tile' id='tile_9_2'></td> <td class='tile' id='tile_9_3'></td> <td class='tile' id='tile_9_4'></td>" +
                    "<td class='tile' id='tile_9_5'></td> <td class='tile' id='tile_9_6'></td> <td class='tile' id='tile_9_7'></td> <td class='tile' id='tile_9_8'></td>" +
                    "<td class='tile' id='tile_9_9'></td> <td class='tile' id='tile_9_10'></td> <td class='tile' id='tile_9_11'></td> <td class='tile' id='tile_9_12'></td>" +
                    "<td class='tile' id='tile_9_13'></td> <td class='tile' id='tile_9_14'></td> <th>9</th>" +
                    "</tr>" +
                    "<tr>" +
                    "<td class='tile' id='tile_8_1'></td> <td class='tile' id='tile_8_2'></td> <td class='tile' id='tile_8_3'></td> <td class='tile' id='tile_8_4'></td>" +
                    "<td class='tile' id='tile_8_5'></td> <td class='tile' id='tile_8_6'></td> <td class='tile' id='tile_8_7'></td> <td class='tile' id='tile_8_8'></td>" +
                    "<td class='tile' id='tile_8_9'></td> <td class='tile' id='tile_8_10'></td> <td class='tile' id='tile_8_11'></td> <td class='tile' id='tile_8_12'></td>" +
                    "<td class='tile' id='tile_8_13'></td> <td class='tile' id='tile_8_14'></td> <th>8</th>" +
                    "</tr>" +
                    "<tr>" +
                    "<td class='tile' id='tile_7_1'></td> <td class='tile' id='tile_7_2'></td> <td class='tile' id='tile_7_3'></td> <td class='tile' id='tile_7_4'></td>" +
                    "<td class='tile' id='tile_7_5'></td> <td class='tile' id='tile_7_6'></td> <td class='tile' id='tile_7_7'></td> <td class='tile' id='tile_7_8'></td>" +
                    "<td class='tile' id='tile_7_9'></td> <td class='tile' id='tile_7_10'></td> <td class='tile' id='tile_7_11'></td> <td class='tile' id='tile_7_12'></td>" +
                    "<td class='tile' id='tile_7_13'></td> <td class='tile' id='tile_7_14'></td> <th>7</th>" +
                    "</tr>" +
                    "<tr>" +
                    "<td class='tile' id='tile_6_1'></td> <td class='tile' id='tile_6_2'></td> <td class='tile' id='tile_6_3'></td> <td class='tile' id='tile_6_4'></td>" +
                    "<td class='tile' id='tile_6_5'></td> <td class='tile' id='tile_6_6'></td> <td class='tile' id='tile_6_7'></td> <td class='tile' id='tile_6_8'></td>" +
                    "<td class='tile' id='tile_6_9'></td> <td class='tile' id='tile_6_10'></td> <td class='tile' id='tile_6_11'></td> <td class='tile' id='tile_6_12'></td>" +
                    "<td class='tile' id='tile_6_13'></td> <td class='tile' id='tile_6_14'></td> <th>6</th>" +
                    "</tr>" +
                    "<tr>" +
                    "<td class='tile' id='tile_5_1'></td> <td class='tile' id='tile_5_2'></td> <td class='tile' id='tile_5_3'></td> <td class='tile' id='tile_5_4'></td>" +
                    "<td class='tile' id='tile_5_5'></td> <td class='tile' id='tile_5_6'></td> <td class='tile' id='tile_5_7'></td> <td class='tile' id='tile_5_8'></td>" +
                    "<td class='tile' id='tile_5_9'></td> <td class='tile' id='tile_5_10'></td> <td class='tile' id='tile_5_11'></td> <td class='tile' id='tile_5_12'></td>" +
                    "<td class='tile' id='tile_5_13'></td> <td class='tile' id='tile_5_14'></td> <th>5</th>" +
                    "</tr>" +
                    "<tr>" +
                    "<td class='tile' id='tile_4_1'></td> <td class='tile' id='tile_4_2'></td> <td class='tile' id='tile_4_3'></td> <td class='tile' id='tile_4_4'></td>" +
                    "<td class='tile' id='tile_4_5'></td> <td class='tile' id='tile_4_6'></td> <td class='tile' id='tile_4_7'></td> <td class='tile' id='tile_4_8'></td>" +
                    "<td class='tile' id='tile_4_9'></td> <td class='tile' id='tile_4_10'></td> <td class='tile' id='tile_4_11'></td> <td class='tile' id='tile_4_12'></td>" +
                    "<td class='tile' id='tile_4_13'></td> <td class='tile' id='tile_4_14'></td> <th>4</th>" +
                    "</tr>" +
                    "<tr>" +
                    "<td class='tile' id='tile_3_1'></td> <td class='tile' id='tile_3_2'></td> <td class='tile' id='tile_3_3'></td> <td class='tile' id='tile_3_4'></td>" +
                    "<td class='tile' id='tile_3_5'></td> <td class='tile' id='tile_3_6'></td> <td class='tile' id='tile_3_7'></td> <td class='tile' id='tile_3_8'></td>" +
                    "<td class='tile' id='tile_3_9'></td> <td class='tile' id='tile_3_10'></td> <td class='tile' id='tile_3_11'></td> <td class='tile' id='tile_3_12'></td>" +
                    "<td class='tile' id='tile_3_13'></td> <td class='tile' id='tile_3_14'></td> <th>3</th>" +
                    "</tr>" +
                    "<tr>" +
                    "<td class='ntile' id='tile_2_1'></td> <td class='ntile' id='tile_2_2'></td> <td class='ntile' id='tile_2_3'></td> <td class='tile' id='tile_2_4'></td>" +
                    "<td class='tile' id='tile_2_5'></td> <td class='tile' id='tile_2_6'></td> <td class='tile' id='tile_2_7'></td> <td class='tile' id='tile_2_8'></td>" +
                    "<td class='tile' id='tile_2_9'></td> <td class='tile' id='tile_2_10'></td> <td class='tile' id='tile_2_11'></td> <td class='tile' id='tile_2_12'></td>" +
                    "<td class='tile' id='tile_2_13'></td> <td class='tile' id='tile_2_14'></td> <th>2</th>" +
                    "</tr>" +
                    "<tr>" +
                    "<td class='ntile' id='tile_1_1'></td> <td class='ntile' id='tile_1_2'></td> <td class='ntile' id='tile_1_3'></td> <td class='tile' id='tile_1_4'></td>" +
                    "<td class='tile' id='tile_1_5'></td> <td class='tile' id='tile_1_6'></td> <td class='tile' id='tile_1_7'></td> <td class='tile' id='tile_1_8'></td>" +
                    "<td class='tile' id='tile_1_9'></td> <td class='tile' id='tile_1_10'></td> <td class='tile' id='tile_1_11'></td> <td class='tile' id='tile_1_12'></td>" +
                    "<td class='tile' id='tile_1_13'></td> <td class='tile' id='tile_1_14'></td> <th>1</th>" +
                    "</tr>" +
                    "</table>");

                // Insert the list onto the screen.
                $('#gameArea').html($str);
                $('#gameArea').append($("<button id='btnBuyCage'>CAGE</button>"));
                $('#gameArea').append($("<button id='btnBuyBooth'>BOOTH</button>"));
                $('#gameArea').append($("<button id='btnBuyDino'>DINO</button>"));
                $('#gameArea').append($("<button id='btnAds'>ADS</button>"));
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
         * Display the countdown timer on the Host screen
         *
         * @param $el The container element for the countdown timer
         * @param startTime
         * @param callback The function to call when the timer ends.
         */
        countDown : function( $el, startTime, callback) {

            // Display the starting time on the screen.
            $el.text(startTime);
            App.doTextFit('#hostWord');

            // console.log('Starting Countdown...');

            // Start a 1 second timer
            var timer = setInterval(countItDown,1000);

            // Decrement the displayed timer value on each 'tick'
            function countItDown(){
                startTime -= 1
                $el.text(startTime);
                App.doTextFit('#hostWord');

                if( startTime <= 0 ){
                    // console.log('Countdown Finished.');

                    // Stop the timer and do the callback.
                    clearInterval(timer);
                    callback();
                    return;
                }
            }

        },

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


