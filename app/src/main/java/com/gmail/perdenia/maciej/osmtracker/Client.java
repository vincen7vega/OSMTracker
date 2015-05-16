package com.gmail.perdenia.maciej.osmtracker;

import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client implements Runnable {

    public static final String TAG = Client.class.getSimpleName();

    private String mServerIp;
    private int mServerPort;
    private MainActivity mContext;
    private User mUser;
    private String mGpx;
    private Protocol mProtocol;

    public Client(String ip, int port, MainActivity context, User user) {
        mServerIp = ip;
        mServerPort = port;
        mContext = context;
        mUser = user;
        mGpx = "";
        mProtocol = new Protocol();
    }

    public Client(String ip, int port, MainActivity context, User user, String gpx) {
        this(ip, port, context, user);
        mGpx = gpx;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        try {
            Socket socket = new Socket(mServerIp, mServerPort);
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(socket.getInputStream());

            String output = mProtocol.processInput(null, mContext, mUser, mGpx);
            out.writeUTF(output);

            String input = in.readUTF();
            mProtocol.processInput(input, mContext, mUser, mGpx);

            socket.close();
        } catch (UnknownHostException e) {
            Log.w(TAG, "Nieznany host " + mServerIp);
            e.printStackTrace();
        } catch (IOException e) {
            Log.w(TAG, "Nie uzyskano I/O do połączenia z hostem: " + mServerIp);
            e.printStackTrace();
        }
    }
}
