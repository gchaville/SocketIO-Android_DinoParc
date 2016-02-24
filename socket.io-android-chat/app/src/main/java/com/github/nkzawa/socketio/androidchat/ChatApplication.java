package com.github.nkzawa.socketio.androidchat;

import android.app.Application;
import io.socket.client.IO;
import io.socket.client.Socket;

import java.net.URISyntaxException;

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
