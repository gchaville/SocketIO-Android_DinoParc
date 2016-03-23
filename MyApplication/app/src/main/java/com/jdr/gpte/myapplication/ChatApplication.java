package com.jdr.gpte.myapplication;

import android.app.Application;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

public class ChatApplication extends Application {

    private String IpAddress;
    private Socket mSocket;

    public ChatApplication(String IP) {
        IpAddress = IP;
        try {
            mSocket = IO.socket(IpAddress);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public Socket getSocket() {
        return mSocket;
    }
}
