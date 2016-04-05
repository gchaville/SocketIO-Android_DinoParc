package com.example.gillian.test_server;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.os.Bundle;
import android.widget.TextView;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIO;
import io.socket.SocketIOException;


public class Connect_after extends Activity {

    private SocketIO mSocket;

    private String mIPaddress;
    private String mUsername;
    private String mGameid;

    final String EXTRA_IP = "ip_address";
    final String EXTRA_NAME = "username";
    final String EXTRA_GAMEID = "gameid";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_after);

        Intent intent = getIntent();

        if(intent != null) {
            mIPaddress = intent.getStringExtra(EXTRA_IP);
            mUsername = intent.getStringExtra(EXTRA_NAME);
            mGameid = intent.getStringExtra(EXTRA_GAMEID);
        }

        try {
            mSocket = new SocketIO(mIPaddress);
            mSocket.connect(
                    new IOCallback() {
                        @Override
                        public void onMessage(JSONObject json, IOAcknowledge ack) {
                            try {
                                System.out.println("Server said:" + json.toString(2));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onMessage(String data, IOAcknowledge ack) {
                            System.out.println("Server said: " + data);
                        }

                        @Override
                        public void onError(SocketIOException socketIOException) {
                            System.out.println("an Error occured");
                            socketIOException.printStackTrace();
                        }

                        @Override
                        public void onDisconnect() {
                            System.out.println("Connection terminated.");
                        }

                        @Override
                        public void onConnect() {
                            System.out.println("Connection established");
                            JSONObject data = new JSONObject();
                            try {
                                data.put("gameId", mGameid);
                                data.put("playerName", mUsername);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            mSocket.emit("playerJoinGame", data);
                        }

                        @Override
                        public void on(String event, IOAcknowledge ack, Object... args) {
                            System.out.println("Server triggered event '" + event + "'");
                            if (event == "yourTurn") {
                                System.out.println("your turn is coming bitch");
                            }
                        }
                    }
            );
        }
        catch (MalformedURLException e1) {
            e1.printStackTrace();
        }

        findViewById(R.id.button_buyCage).setOnClickListener(mGlobal_OnClickListener);
        findViewById(R.id.button_buyDino).setOnClickListener(mGlobal_OnClickListener);
        findViewById(R.id.button_buyBooth).setOnClickListener(mGlobal_OnClickListener);

    }

    //Global On click listener for all views
    final View.OnClickListener mGlobal_OnClickListener = new View.OnClickListener() {
        public void onClick(final View v) {
            TextView actionDisplay = (TextView)findViewById(R.id.action_display);
            JSONObject data = new JSONObject();
            switch(v.getId()) {
                case R.id.button_buyCage:
                    try {
                        data.put("gameId", mGameid);
                        data.put("playerName", mUsername);
                        data.put("action", "playerBuyCage");
                        data.put("coordX", 5);
                        data.put("coordY", 5);
                        data.put("playerId", mSocket.getSessionId());
                    }catch (JSONException e) {
                        e.printStackTrace();
                    }

                    mSocket.emit("playerAction", data);
                    actionDisplay.setText("Player buy a cage");
                    break;

                case R.id.button_buyDino:
                    try {
                        data.put("gameId", mGameid);
                        data.put("playerName", mUsername);
                        data.put("action", "playerBuyDino");
                        data.put("coordX", 5);
                        data.put("coordY", 5);
                        data.put("dinoType", "Velociraptor");
                        data.put("playerId",mSocket.getSessionId());
                    }catch (JSONException e) {
                        e.printStackTrace();
                    }

                    mSocket.emit("playerAction", data);
                    actionDisplay.setText("Player buy a dino");
                    break;

                case R.id.button_buyBooth:
                    try {
                        data.put("gameId", mGameid);
                        data.put("playerName", mUsername);
                        data.put("action", "playerBuyBooth");
                        data.put("coordX", 5);
                        data.put("coordY", 5);
                        data.put("boothType", "Spy");
                        data.put("playerId",mSocket.getSessionId());
                    }catch (JSONException e) {
                        e.printStackTrace();
                    }

                    mSocket.emit("playerAction", data);
                    actionDisplay.setText("Player buy a booth");
                    break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mSocket.disconnect();
    }
}
