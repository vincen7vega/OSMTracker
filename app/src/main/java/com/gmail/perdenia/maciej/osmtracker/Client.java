package com.gmail.perdenia.maciej.osmtracker;

import android.util.Log;
import android.widget.Toast;

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

public class Client implements Runnable {

    public static final String TAG = Client.class.getSimpleName();

    private MainActivity mContext;
    private String mServerIp;
    private int mServerPort;
    private User mUser;
    private String mGpx;
    private boolean mSending;

    public Client(MainActivity context, String ip, int port, User user) {
        mContext = context;
        mServerIp = ip;
        mServerPort = port;
        mUser = user;
        mSending = false;
        mGpx = "";
    }

    public Client(MainActivity context, String ip, int port, User user, String gpx) {
        this(context, ip, port, user);
        mGpx = gpx;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        try {
            Socket client = new Socket(mServerIp, mServerPort);

            OutputStream outToServer = client.getOutputStream();
            DataOutputStream out = new DataOutputStream(outToServer);

            if (!mGpx.equals("")) {
                mSending = true;

                JSONObject jsonToSend = new JSONObject();
                jsonToSend.put("id", mUser.getId());
                jsonToSend.put("name", mUser.getName());
                jsonToSend.put("surname", mUser.getSurname());
                jsonToSend.put("time", mUser.getWayPoint().getTimeString());
                jsonToSend.put("gpx", mGpx);
                String jsonToSendString = jsonToSend.toString();
                out.writeUTF(jsonToSendString);

                InputStream inFromServer = client.getInputStream();
                DataInputStream in = new DataInputStream(inFromServer);
                Log.i(TAG, "Odpowiedź serwera: " + in.readUTF());

                mSending = false;

                mContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(
                                mContext, "Plik zapisano na serwerze", Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                JSONObject jsonToSend = new JSONObject();
                jsonToSend.put("id", mUser.getId());
                jsonToSend.put("name", mUser.getName());
                jsonToSend.put("surname", mUser.getSurname());
                JSONObject jsonLocation = new JSONObject();
                jsonLocation.put("latitude", mUser.getWayPoint().getLatitude());
                jsonLocation.put("longitude", mUser.getWayPoint().getLongitude());
                jsonToSend.put("location", jsonLocation);
                jsonToSend.put("time", mUser.getWayPoint().getTimeString());
                String jsonToSendString = jsonToSend.toString();
                out.writeUTF(jsonToSendString);
                Log.i(TAG, "JSON wysłany: " + jsonToSendString);

                InputStream inFromServer = client.getInputStream();
                DataInputStream in = new DataInputStream(inFromServer);
                String jsonStringReceived = in.readUTF();
                Log.i(TAG, "JSON odebrany: " + jsonStringReceived);

                JSONParser parser = new JSONParser();
                Object object = parser.parse(jsonStringReceived);
                JSONArray jsonArrayReceived = (JSONArray) object;

                ArrayList<User> otherUsers = new ArrayList<>();
                for (int i = 0; i < jsonArrayReceived.size(); i++) {
                    JSONObject userReceived = (JSONObject) jsonArrayReceived.get(i);
                    long idReceived = (long) userReceived.get("id");
                    String nameReceived = (String) userReceived.get("name");
                    String surnameReceived = (String) userReceived.get("surname");
                    JSONObject locationReceived = (JSONObject) userReceived.get("location");
                    double latitudeReceived = (double) locationReceived.get("latitude");
                    double longitudeReceived = (double) locationReceived.get("longitude");
                    String timeReceivedString = (String) userReceived.get("time");
                    WayPoint wayPoint = new WayPoint(latitudeReceived, longitudeReceived,
                            timeReceivedString);
                    otherUsers.add(
                            new User((int) idReceived, nameReceived, surnameReceived, wayPoint));
                }

                for(User u : otherUsers) {
                    Log.i(TAG, "ID" + u.getId() + " " + u.getName() + " " + u.getSurname() + ", " +
                            u.getWayPoint().getLatitude() + "/" + u.getWayPoint().getLongitude() +
                            ", " + u.getWayPoint().getTimeString());
                }

                mContext.setOtherUsers(otherUsers);
            }

            client.close();
        } catch (UnknownHostException e) {
            Log.w(TAG, "Złapano wyjątek (Unknown Host Exception)");
            e.printStackTrace();
            showFailureToast();
        } catch (IOException e) {
            Log.w(TAG, "Złapano wyjątek (IO Exception)");
            e.printStackTrace();
            showFailureToast();
        } catch (ParseException e) {
            Log.w(TAG, "Złapano wyjątek (Parse Exception), pozycja: " + e.getPosition());
            e.printStackTrace();
        }
    }

    private void showFailureToast() {
        if (!mGpx.equals("")) {
            mContext.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mSending) {
                        Toast.makeText(mContext, "Niepowodzenie - błąd wysyłania pliku",
                                Toast.LENGTH_LONG).show();
                        mSending = false;
                    } else {
                        Toast.makeText(mContext, "Niepowodzenie - błąd połączenia z serwerem",
                                Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
}
