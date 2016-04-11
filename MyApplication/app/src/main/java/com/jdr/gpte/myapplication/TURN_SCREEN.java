package com.jdr.gpte.myapplication;

import android.app.Activity;
import android.net.Uri;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import android.util.Log;

public class TURN_SCREEN extends AppCompatActivity {
    private MySocket mSocketService;
    private boolean mServiceBound = false;

    private String mUsername;
    private String mGameid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_turn__screen);
        Log.i("TurnScreen", "onCreate");

        Intent intent = getIntent();

        if(intent != null) {
            mUsername = intent.getExtras().getString(Constants.EXTRA_NAME);
            mGameid = intent.getExtras().getString(Constants.EXTRA_GAMEID);
        }

        Intent serviceIntent = new Intent(TURN_SCREEN.this, MySocket.class);
        bindService(serviceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.PLAYER_TURN);

        registerReceiver(mReceiver, filter);

    }

    @Override
    protected  void onStart() {
        super.onStart();
        Log.i("TurnScreen", "onStart");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i("TurnScreen", "onRestart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i("TurnScreen", "onStop");
        if(mServiceBound) {
            unbindService(mServiceConnection);
            mServiceBound = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("TurnScreen", "onDestroy");
        unregisterReceiver(mReceiver);
    }

    private void attemptTurn() {
        Bundle infos = new Bundle();
        infos.putString(Constants.EXTRA_NAME, mUsername);
        infos.putString(Constants.EXTRA_GAMEID, mGameid);

        Intent intent = new Intent(TURN_SCREEN.this, MainActivity.class);
        intent.putExtras(infos);
        startActivity(intent);
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

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent.getAction().equals(Constants.PLAYER_TURN)) {
                Log.i("TurnScreen", "turn is coming 1");
                createButtonReady();
                createNotification(context);
            }
        }
    };

    private final void createButtonReady() {
        final RelativeLayout lm = (RelativeLayout) findViewById(R.id.turn_layout);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        final Button btn = new Button(TURN_SCREEN.this);
        // Give button an ID
        btn.setId(R.id.turn_ready);
        btn.setText(R.string.action_turn);
        // set the layoutParams on the button
        btn.setLayoutParams(params);
        // Set click listener for button
        btn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Bundle infos = new Bundle();
                infos.putString(Constants.EXTRA_NAME, mUsername);
                infos.putString(Constants.EXTRA_GAMEID, mGameid);

                Intent intent = new Intent(TURN_SCREEN.this, MainActivity.class);
                intent.putExtras(infos);
                startActivity(intent);
                lm.removeView(findViewById(R.id.turn_ready));
            }
        });

        //Add button to LinearLayout
        lm.addView(btn);
    }

    private final void createNotification(Context context) {
        // prepare intent which is triggered if the
        // notification is selected

        Bundle infos = new Bundle();
        infos.putString(Constants.EXTRA_NAME, mUsername);
        infos.putString(Constants.EXTRA_GAMEID, mGameid);

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtras(infos);
        // use System.currentTimeMillis() to have a unique ID for the pending intent
        PendingIntent pIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), intent, 0);

        // build notification
        // the addAction re-use the same intent to keep the example short
        Notification n  = new Notification.Builder(context)
                .setContentTitle("DinoParc")
                .setContentText("It's your turn !")
                .setSmallIcon(R.drawable.tyrannosaure)
                .setContentIntent(pIntent)
                .setSound(Uri.parse("android.resource://com.jdr.gpte.myapplication/" + R.raw.bell)).build();

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // hide the notification after its selected
        n.flags |= Notification.FLAG_AUTO_CANCEL;
        n.defaults |= Notification.DEFAULT_VIBRATE;

        notificationManager.notify(0, n);
    }
}
