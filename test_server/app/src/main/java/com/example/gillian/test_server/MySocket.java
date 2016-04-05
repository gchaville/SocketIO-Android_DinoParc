package com.example.gillian.test_server;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Binder;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;

import io.socket.SocketIO;
import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIO;
import io.socket.SocketIOException;



public class MySocket extends Service {

    private SocketIO mSocket;

    private String mIPaddress;
    private String mUsername;
    private String mGameid;

    public MySocket(String ipAddress, String playerName, String GameId) {
        mIPaddress = ipAddress; mUsername= playerName; mGameid = GameId;
    }

    @Override
    public void onCreate() {
        try {
            mSocket = new SocketIO(mIPaddress);
            mSocket.connect(new IOCallback() {
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
                }

                @Override
                public void on(String event, IOAcknowledge ack, Object... args) {
                    System.out.println("Server triggered event '" + event + "'");
                }
            });
        }
        catch (MalformedURLException e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mSocket != null) {
            mSocket.disconnect();
        } else {
            try {
                mSocket = new SocketIO(mIPaddress);
                mSocket.connect(new IOCallback() {
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
                    }

                    @Override
                    public void on(String event, IOAcknowledge ack, Object... args) {
                        System.out.println("Server triggered event '" + event + "'");
                    }

                });
            }
            catch (MalformedURLException e1) {
                e1.printStackTrace();
            }
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public class LocalBinder extends Binder {
        SocketIO getSocket() {
            return mSocket;
        }

        MySocket getService() {
            return MySocket.this;
        }
    }


}
