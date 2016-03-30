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

    private SocketIO mSocket;
    private EditText mUsernameView;

    private String mUsername;

    private String mIPaddress;

    final String EXTRA_IP = "ip_address";
    final String EXTRA_NAME = "username";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        Intent intent = getIntent();
        mIPaddress = intent.getStringExtra(EXTRA_IP);

        TextView addressDisplay = (TextView) findViewById(R.id.ipaddress_display);


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
                            }

                            @Override
                            public void on(String event, IOAcknowledge ack, Object... args) {
                                System.out.println("Server triggered event '" + event + "'");
                            }
                        }
                );
            }
            catch (MalformedURLException e1) {
                    e1.printStackTrace();
            }

        // Set up the login form.
        mUsernameView = (EditText) findViewById(R.id.username_input);

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

        mSocket.disconnect();
    }
    /**
     * Attempts to sign in the account specified by the login form.
     * If there are form errors (invalid username, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        // Reset errors.
        mUsernameView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUsernameView.getText().toString().trim();

        // Check for a valid username.
        if (TextUtils.isEmpty(username)) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            mUsernameView.setError(getString(R.string.error_field_required));
            mUsernameView.requestFocus();
            return;
        }

        mUsername = username;

        Intent intent = new Intent(LoginActivity.this, Connect_after.class);
        intent.putExtra(EXTRA_IP, mIPaddress);
        intent.putExtra(EXTRA_NAME, mUsername);
        startActivity(intent);
        // perform the user login attempt.
       mSocket.emit("add user", username);
    }


    /*private Emitter.Listener onLogin = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];

            int numUsers;
            try {
                numUsers = data.getInt("numUsers");
            } catch (JSONException e) {
                return;
            }

            Intent intent = new Intent();
            intent.putExtra("username", mUsername);
            intent.putExtra("numUsers", numUsers);
            setResult(RESULT_OK, intent);
            finish();
        }
    };*/
}



