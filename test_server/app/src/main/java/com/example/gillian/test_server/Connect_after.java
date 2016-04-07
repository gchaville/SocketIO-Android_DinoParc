package com.example.gillian.test_server;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.view.View;
import android.os.Bundle;
import android.widget.TextView;
import android.util.Log;

public class Connect_after extends Activity {

    private MySocket mSocketService;
    private boolean mServiceBound = false;

    private String mUsername;
    private String mGameid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_after);

        Log.i("Connect_afta","onCreate");
        Intent intent = getIntent();

        if(intent != null) {
            mUsername = intent.getExtras().getString(Constants.EXTRA_NAME);
            mGameid = intent.getExtras().getString(Constants.EXTRA_GAMEID);
        }

        findViewById(R.id.button_buyCage).setOnClickListener(mGlobal_OnClickListener);
        findViewById(R.id.button_buyDino).setOnClickListener(mGlobal_OnClickListener);
        findViewById(R.id.button_buyBooth).setOnClickListener(mGlobal_OnClickListener);

        Intent serviceIntent = new Intent(Connect_after.this, MySocket.class);
        bindService(serviceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

    }

    //Global On click listener for all views
    final View.OnClickListener mGlobal_OnClickListener = new View.OnClickListener() {
        public void onClick(final View v) {
            TextView actionDisplay = (TextView)findViewById(R.id.action_display);
            Intent emitIntent = new Intent();
            emitIntent.setAction(Constants.PLAYER_ACTION);

            switch(v.getId()) {
                case R.id.button_buyCage:
                    emitIntent.putExtra("gameId", mGameid);
                    emitIntent.putExtra("playerName", mUsername);
                    emitIntent.putExtra("action", "playerBuyCage");
                    emitIntent.putExtra("coordX", 5);
                    emitIntent.putExtra("coordY", 5);
                    actionDisplay.setText("Player buy a cage");
                    break;

                case R.id.button_buyDino:
                    emitIntent.putExtra("gameId", mGameid);
                    emitIntent.putExtra("playerName", mUsername);
                    emitIntent.putExtra("action", "playerBuyDino");
                    emitIntent.putExtra("coordX", 5);
                    emitIntent.putExtra("coordY", 5);
                    actionDisplay.setText("Player buy a dino");
                    break;

                case R.id.button_buyBooth:
                    emitIntent.putExtra("gameId", mGameid);
                    emitIntent.putExtra("playerName", mUsername);
                    emitIntent.putExtra("action", "playerBuyBooth");
                    emitIntent.putExtra("coordX", 5);
                    emitIntent.putExtra("coordY", 5);
                    actionDisplay.setText("Player buy a booth");
                    break;
            }
            sendBroadcast(emitIntent);
            Bundle infos = new Bundle();
            infos.putString(Constants.EXTRA_NAME, mUsername);
            infos.putString(Constants.EXTRA_GAMEID, mGameid);

            Intent intent = new Intent(Connect_after.this, TurnScreen.class);
            intent.putExtras(infos);
            startActivity(intent);
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        Log.i("Connect_afta", "onStart");
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notificationManager.cancel(0);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i("Connect_afta", "onStop");
        if(mServiceBound) {
            unbindService(mServiceConnection);
            mServiceBound = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("Connect_afta", "onDestroy");
        //mSocket.disconnect();
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MySocket.MyBinder myBinder = (MySocket.MyBinder) service;
            mSocketService = myBinder.getService();
            mServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceBound = false;
        }
    };

}
