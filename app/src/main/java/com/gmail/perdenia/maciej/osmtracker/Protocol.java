package com.gmail.perdenia.maciej.osmtracker;

import android.util.Log;
import android.widget.Toast;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;

public class Protocol {

    private static final String GPX_SUCCESS_CODE = "gpx-success";

    @SuppressWarnings("unchecked")
    public String processInput(String input, final MainActivity context, User user, String gpx) {
        String output = null;

        try {
            if (input == null) {
                if (!gpx.equals("")) {
                    JSONObject jsonToSend = new JSONObject();
                    jsonToSend.put("id", user.getId());
                    jsonToSend.put("name", user.getName());
                    jsonToSend.put("surname", user.getSurname());
                    jsonToSend.put("time", user.getWayPoint().getTimeString());
                    jsonToSend.put("gpx", gpx);

                    output = jsonToSend.toString();
                } else {
                    JSONObject jsonToSend = new JSONObject();
                    jsonToSend.put("id", user.getId());
                    jsonToSend.put("name", user.getName());
                    jsonToSend.put("surname", user.getSurname());
                    JSONObject jsonLocation = new JSONObject();
                    jsonLocation.put("latitude", user.getWayPoint().getLatitude());
                    jsonLocation.put("longitude", user.getWayPoint().getLongitude());
                    jsonToSend.put("location", jsonLocation);
                    jsonToSend.put("time", user.getWayPoint().getTimeString());

                    output = jsonToSend.toString();
                }
            } else {
                if (!gpx.equals("")) {
                    Log.i(Client.TAG, "Odpowiedü serwera: " + input);
                    if (input.equals(GPX_SUCCESS_CODE)) {
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(
                                        context, "Plik zapisano na serwerze", Toast.LENGTH_LONG)
                                        .show();
                            }
                        });
                    }
                } else {
                    JSONParser parser = new JSONParser();
                    Object object = parser.parse(input);
                    JSONArray jsonReceived = (JSONArray) object;

                    ArrayList<User> otherUsers = new ArrayList<>();
                    for (int i = 0; i < jsonReceived.size(); i++) {
                        JSONObject userReceived = (JSONObject) jsonReceived.get(i);
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
                                new User((int) idReceived, nameReceived, surnameReceived,
                                        wayPoint));
                    }

                    for(User u : otherUsers) {
                        Log.i(Client.TAG, "ID" + u.getId() + " " + u.getName() + " " +
                                u.getSurname() + ", " + u.getWayPoint().getLatitude() + "/" +
                                u.getWayPoint().getLongitude() + ", " +
                                u.getWayPoint().getTimeString());
                    }
                    context.setOtherUsers(otherUsers);
                }
            }
        } catch (ParseException e) {
            Log.w(Client.TAG, "Z≥apano wyjπtek (ParseException): " + e.toString() + ", pozycja: " +
                    e.getPosition());
            e.printStackTrace();
        }

        return output;
    }
}
