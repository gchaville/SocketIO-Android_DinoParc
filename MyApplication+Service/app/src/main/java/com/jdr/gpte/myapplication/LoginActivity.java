package com.jdr.gpte.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;


/**
 * A login screen that offers login via username.
 */
public class LoginActivity extends Activity {

    private MySocket mSocketService;
    private boolean mServiceBound = false;

    private EditText mUsernameView;
    private EditText mIPaddressView;
    private EditText mGameidView;

    private String mIPaddress;
    private String mUsername;
    private String mGameid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Set up the login form.
        mUsernameView = (EditText) findViewById(R.id.username_input);
        mIPaddressView = (EditText) findViewById(R.id.ipaddress_input);
        mGameidView = (EditText) findViewById(R.id.gameid_input);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.GAME_STARTED);
        registerReceiver(mReceiver, filter);

        Button signInButton = (Button) findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

    }

    /**
     * Attempts to sign in the account specified by the login form.
     * If there are form errors (invalid username, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        // Reset errors.
        mUsernameView.setError(null);
        mIPaddressView.setError(null);
        mGameidView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUsernameView.getText().toString().trim();
        String ipaddress = mIPaddressView.getText().toString().trim();
        String gameid = mGameidView.getText().toString().trim();

        // Check for a valid username.
        if (TextUtils.isEmpty(username)) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            mUsernameView.setError(getString(R.string.error_field_required));
            mUsernameView.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(ipaddress)) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            mIPaddressView.setError(getString(R.string.error_field_required));
            mIPaddressView.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(gameid)) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            mGameidView.setError(getString(R.string.error_field_required));
            mGameidView.requestFocus();
            return;
        }

        mIPaddress = "http://" + ipaddress + "/";
        mUsername = username;
        mGameid = gameid;

        Bundle infos = new Bundle();
        infos.putString(Constants.EXTRA_IP, mIPaddress);
        infos.putString(Constants.EXTRA_NAME, mUsername);
        infos.putString(Constants.EXTRA_GAMEID, mGameid);

        Intent serviceIntent = new Intent(LoginActivity.this, MySocket.class);
        serviceIntent.putExtras(infos);
        startService(serviceIntent);
        bindService(serviceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

        waitPopUp();


    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mServiceBound) {
            unbindService(mServiceConnection);
            mServiceBound = false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        if(mServiceBound) {
            unbindService(mServiceConnection);
            mServiceBound = false;
        }
        stopService(new Intent(LoginActivity.this, MySocket.class));
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constants.GAME_STARTED)) {
                Log.i("LoginAct", "in BroadcastReceiver : game started " + mUsername + " " + mGameid);
                Bundle infos = new Bundle();
                infos.putString(Constants.EXTRA_NAME, mUsername);
                infos.putString(Constants.EXTRA_GAMEID, mGameid);
                //infos.putBoolean(Constants.EXTRA_NEWTURN, false);

                Intent Turnintent = new Intent(LoginActivity.this, MainActivity.class);
                Turnintent.putExtras(infos);
                startActivity(Turnintent);
            }
        }
    };

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

    private void waitPopUp() {
        AlertDialog.Builder popUpMenu = new AlertDialog.Builder(this);
        popUpMenu.setTitle("Waiting");
        popUpMenu.setMessage("Joined Game " + mGameid + ".\n Please wait for game to begin.");
        popUpMenu.create().show();
    }
}