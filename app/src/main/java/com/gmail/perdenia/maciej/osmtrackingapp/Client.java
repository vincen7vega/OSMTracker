package com.gmail.perdenia.maciej.osmtrackingapp;

import android.content.Context;
import android.location.Location;
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
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class Client implements Runnable {

    public static final String TAG = Client.class.getSimpleName();

    private static final int SERVER_PORT = 12345;
    private static final String SERVER_IP = "192.168.1.13";

    private MainActivity mContext;
    private User mUser;

    public Client(MainActivity context, User user) {
        mContext = context;
        mUser = user;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        try {
            Socket client = new Socket(SERVER_IP, SERVER_PORT);

            OutputStream outToServer = client.getOutputStream();
            DataOutputStream out = new DataOutputStream(outToServer);

            JSONObject jsonToSend = new JSONObject();
            jsonToSend.put("name", mUser.getName());
            jsonToSend.put("surname", mUser.getSurname());
            JSONObject jsonLocation = new JSONObject();
            jsonLocation.put("latitude", mUser.getLocation().getLatitude());
            jsonLocation.put("longitude", mUser.getLocation().getLongitude());
            jsonToSend.put("location", jsonLocation);
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
                Location location = new Location("dummyprovider");
                location.setLatitude(latitudeReceived);
                location.setLongitude(longitudeReceived);
                otherUsers.add(new User(nameReceived, surnameReceived, location));
            }

            for(User ou : otherUsers) {
                Log.i(TAG, ou.getName() + " " + ou.getSurname() + ", "
                + ou.getLocation().getLatitude() + "/" + ou.getLocation().getLongitude());
            }

            mContext.setOtherUsers(otherUsers);

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
