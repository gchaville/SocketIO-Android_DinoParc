package com.example.gillian.test_server;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIO;
import io.socket.SocketIOException;


/**
 * A login screen that offers login via username.
 */
public class LoginActivity extends Activity {

    private EditText mUsernameView;
    private EditText mIPaddressView;
    private EditText mGameidView;

    private String mIPaddress;
    private String mUsername;
    private String mGameid;

    final String EXTRA_IP = "ip_address";
    final String EXTRA_NAME = "username";
    final String EXTRA_GAMEID = "gameid";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Set up the login form.
        mUsernameView = (EditText) findViewById(R.id.username_input);
        mIPaddressView = (EditText) findViewById(R.id.ipaddress_input);
        mGameidView = (EditText) findViewById(R.id.gameid_input);

        Button signInButton = (Button) findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

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

        mIPaddress = "http://"+ipaddress+"/";
        mUsername = username;
        mGameid = gameid;

        Intent intent = new Intent(LoginActivity.this, Connect_after.class);
        intent.putExtra(EXTRA_IP, mIPaddress);
        intent.putExtra(EXTRA_NAME, mUsername);
        intent.putExtra(EXTRA_GAMEID, mGameid);
        startActivity(intent);

    }

}



