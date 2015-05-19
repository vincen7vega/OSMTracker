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
    private IOHandler mIOHandler;

    public Client(String ip, int port, MainActivity context, User user) {
        mServerIp = ip;
        mServerPort = port;
        mContext = context;
        mUser = user;
        mGpx = "";
        mIOHandler = new IOHandler();
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

            String output = mIOHandler.createOutput(mUser, mGpx);

            int length = output.length();
            if (length <= 16 * 1024) {
                out.writeUTF(output);
                out.writeUTF("end");
            } else {
                int start = 0;
                int end = 16 * 1024;
                while(start < length) {
                    String subOutput = output.substring(start, end);
                    out.writeUTF(subOutput);
                    start = end;
                    end = start + 16 * 1024;
                    if (end > length) {
                        end = length;
                    }
                }
                out.writeUTF("end");
            }

            String input = "";
            String subInput;
            while (true) {
                subInput = in.readUTF();
                if (subInput.equals("end")) break;
                input += subInput;
            }

            mIOHandler.processInput(input, mContext, mGpx);

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
