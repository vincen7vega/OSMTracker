package com.gmail.perdenia.maciej.osmtrackingapp;

import android.util.Log;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class Client implements Runnable {

    public static final String TAG = Client.class.getSimpleName();

    private static final int SERVER_PORT = 60000;
    private static final String SERVER_IP = "192.168.1.13";

    private MainActivity mContext;
    private User mUser;
    private String mGpx;

    public Client(MainActivity context, User user) {
        mContext = context;
        mUser = user;
        mGpx = "";
    }

    public Client(MainActivity context, User user, String gpx) {
        mContext = context;
        mUser = user;
        mGpx = gpx;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        try {
            Socket client = new Socket(InetAddress.getByName(SERVER_IP), SERVER_PORT);

            OutputStream outToServer = client.getOutputStream();
            DataOutputStream out = new DataOutputStream(outToServer);

            if (!mGpx.equals("")) {
                JSONObject jsonToSend = new JSONObject();
                jsonToSend.put("name", mUser.getName());
                jsonToSend.put("surname", mUser.getSurname());
                jsonToSend.put("time", mUser.getWayPoint().getTimeString());
                jsonToSend.put("gpx", mGpx);
                String jsonToSendString = jsonToSend.toString();
                out.writeUTF(jsonToSendString);

                InputStream inFromServer = client.getInputStream();
                DataInputStream in = new DataInputStream(inFromServer);
                Log.i(TAG, "Odpowiedź serwera: " + in.readUTF());
            } else {
                JSONObject jsonToSend = new JSONObject();
                jsonToSend.put("name", mUser.getName());
                jsonToSend.put("surname", mUser.getSurname());
                JSONObject jsonLocation = new JSONObject();
                jsonLocation.put("latitude", mUser.getWayPoint().getLatitude());
                jsonLocation.put("longitude", mUser.getWayPoint().getLongitude());
                jsonToSend.put("location", jsonLocation);
                jsonToSend.put("time", mUser.getWayPoint().getTimeString());
                String jsonToSendString = jsonToSend.toString();
                // Log.i(TAG, "JSON wysłany: " + jsonToSendString);
                out.writeUTF(jsonToSendString);

                InputStream inFromServer = client.getInputStream();
                DataInputStream in = new DataInputStream(inFromServer);
                String jsonStringReceived = in.readUTF();
                // Log.i(TAG, "JSON odebrany: " + in.readUTF());

                JSONParser parser = new JSONParser();
                Object object = parser.parse(jsonStringReceived);
                JSONArray jsonArrayReceived = (JSONArray) object;

                List<User> otherUsers = new ArrayList<>();
                for (int i = 0; i < jsonArrayReceived.size(); i++) {
                    JSONObject userReceived = (JSONObject) jsonArrayReceived.get(i);
                    String nameReceived = (String) userReceived.get("name");
                    String surnameReceived = (String) userReceived.get("surname");
                    JSONObject locationReceived = (JSONObject) userReceived.get("location");
                    double latitudeReceived = (double) locationReceived.get("latitude");
                    double longitudeReceived = (double) locationReceived.get("longitude");
                    String timeReceivedString = (String) userReceived.get("time");
                    WayPoint wayPoint = new WayPoint(latitudeReceived, longitudeReceived,
                            timeReceivedString);
                    otherUsers.add(new User(nameReceived, surnameReceived, wayPoint));
                }

                for(User ou : otherUsers) {
                    Log.i(TAG, ou.getName() + " " + ou.getSurname() + ", "
                            + ou.getWayPoint().getLatitude() + "/" + ou.getWayPoint().getLongitude()
                            + ", " + ou.getWayPoint().getTimeString());
                }

                mContext.setOtherUsers(otherUsers);
            }

            client.close();
        } catch (UnknownHostException e) {
            Log.w(TAG, "Unknown Host Exception");
            e.printStackTrace();
        } catch (IOException e) {
            Log.w(TAG, "IO Exception");
            e.printStackTrace();
        } catch (ParseException pe) {
            Log.w(TAG, "Parse Exception, pozycja: " + pe.getPosition());
            Log.w(TAG, pe.toString());
            pe.printStackTrace();
        }
    }
}
