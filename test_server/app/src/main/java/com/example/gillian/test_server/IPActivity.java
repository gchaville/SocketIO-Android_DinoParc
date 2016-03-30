package com.example.gillian.test_server;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

/**
 * A login screen that offers login via username.
 */
public class IPActivity extends Activity {

    private EditText mIPaddressView;

    private String mIPaddress;

    final String EXTRA_IP = "ip_address";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ipaddress);

        // Set up the login form.
        mIPaddressView = (EditText) findViewById(R.id.ipaddress_input);

        Button signInButton = (Button) findViewById(R.id.connect_button);
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
        mIPaddressView.setError(null);

        // Store values at the time of the login attempt.
        String ipaddress = mIPaddressView.getText().toString().trim();

        // Check for a valid username.
        if (TextUtils.isEmpty(ipaddress)) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            mIPaddressView.setError(getString(R.string.error_field_required));
            mIPaddressView.requestFocus();
            return;
        }

        mIPaddress = "http://"+ipaddress+"/";
        Intent intent = new Intent(IPActivity.this, LoginActivity.class);
        intent.putExtra(EXTRA_IP, mIPaddress);
        startActivity(intent);
    }
}



