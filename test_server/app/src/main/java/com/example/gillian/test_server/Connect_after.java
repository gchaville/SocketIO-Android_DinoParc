package com.example.gillian.test_server;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class Connect_after extends Activity {

    final String EXTRA_IP = "ip_address";
    final String EXTRA_NAME = "username";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_after);

        TextView addressDisplay = (TextView) findViewById(R.id.ipaddress_display);
        TextView nameDisplay = (TextView) findViewById(R.id.name_display);

        Intent intent = getIntent();

        if(intent != null) {
            addressDisplay.setText(intent.getStringExtra(EXTRA_IP));
            nameDisplay.setText(intent.getStringExtra(EXTRA_NAME));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}
