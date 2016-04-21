package com.jdr.gpte.myapplication;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.Binder;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;

import io.socket.SocketIO;
import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIOException;




public class MySocket extends Service {

    private static String LOG_TAG = "BoundSocketService";
    private IBinder mBinder = new MyBinder();

    private SocketIO mSocket;

    private String mIPaddress;
    private String mUsername;
    private String mGameid;

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.PLAYER_ACTION);
        filter.addAction(Constants.PLAYER_INFO);

        registerReceiver(mReceiver, filter);
        Log.i(LOG_TAG, "in onCreate");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(LOG_TAG, "in onBind");
        if (intent != null) {
            mIPaddress = intent.getExtras().getString(Constants.EXTRA_IP);
            mUsername = intent.getExtras().getString(Constants.EXTRA_NAME);
            mGameid = intent.getExtras().getString(Constants.EXTRA_GAMEID);
            Log.i(LOG_TAG, mIPaddress + " " + mUsername + " " + mGameid);

            if (mSocket != null) {
                mSocket.disconnect();
            }
            else {
                try {
                    mSocket = new SocketIO(mIPaddress, new IOCallback() {
                        @Override
                        public void onMessage(JSONObject json, IOAcknowledge ack) {}

                        @Override
                        public void onMessage(String data, IOAcknowledge ack) {}

                        @Override
                        public void onError(SocketIOException socketIOException) {
                            System.out.println("an Error occured");
                            socketIOException.printStackTrace();
                            stopSelf();
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
                            mSocket.emit(Constants.PLAYER_CONNECTED, data);
                        }

                        @Override
                        public void on(String event, IOAcknowledge ack, Object... args) {
                            //System.out.println("Server triggered event '" + event + "'");
                            if (event.equals(Constants.PLAYER_TURN)) {
                                JSONObject data = (JSONObject) args[0];
                                try {
                                    if (data.getString("playerName").equals(mUsername)) {
                                        Log.i(LOG_TAG, "your turn is coming bitch");
                                        notifyPlayer(Constants.PLAYER_TURN);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } else if (event.equals(Constants.GAME_STARTED)) {
                                JSONObject data = (JSONObject) args[0];
                                try {
                                    if (data.getString("gameId").equals(mGameid)) {
                                        Log.i(LOG_TAG, "game started");
                                        notifyPlayer(Constants.GAME_STARTED);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                } catch (MalformedURLException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.i(LOG_TAG, "in onRebind");
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(LOG_TAG, "in onUnbind");
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSocket.disconnect();
        unregisterReceiver(mReceiver);
        Log.i(LOG_TAG, "in onDestroy");
        stopSelf();
    }

    public class MyBinder extends Binder {
        MySocket getService() {
            return MySocket.this;
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            JSONObject data = new JSONObject();

            if(intent.getAction().equals(Constants.PLAYER_ACTION)) {
                Log.i(LOG_TAG, "in BroadcastReceiver : emit playerAction ");
                try {
                    data.put("gameId", intent.getStringExtra("gameId"));
                    data.put("playerName", intent.getStringExtra("playerName"));
                    data.put("action", intent.getStringExtra("action"));
                    data.put("coordX", intent.getIntExtra("coordX",0));
                    data.put("coordY", intent.getIntExtra("coordY",0));
                    data.put("playerId", mSocket.getSessionId());
                }
                catch(JSONException e) {
                    e.printStackTrace();
                }
                mSocket.emit(Constants.PLAYER_ACTION, data);
            }
            /*else if (intent.getAction().equals(Constants.PLAYER_INFO)) {
                Log.i(LOG_TAG, "in BroadcastReceiver : emit playerInfo");
                mSocket.emit(Constants.PLAYER_INFO, data);
            }*/
        }
    };


    private void notifyPlayer(String notif) {
        Intent emitIntent = new Intent();
        emitIntent.setAction(notif);
        sendBroadcast(emitIntent);
    }
}
