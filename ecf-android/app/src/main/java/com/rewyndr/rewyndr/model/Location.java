package com.rewyndr.rewyndr.model;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class Location {
    private static String TAG = "Location";

    // Members
    private int id;
    private String name;

    // Constructors
    public Location(JSONObject data) {
        try {
            this.id = data.getInt("id");
            this.name = data.getString("name");
        } catch(JSONException e) {
            Log.e(TAG, "Error deserializing location: " + e.getMessage());
        }

    }

    public static ArrayList<Location> deserializeLocations(String json) {
        ArrayList<Location> locations = new ArrayList<>();
        JSONArray a;

        try {
            a = new JSONArray(json);
            for(int i = 0; i < a.length(); i++) {
                locations.add(new Location(a.getJSONObject(i)));
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error deserializing locations: " + e.getMessage());
        }

        return locations;
    }

    // Instance methods
    public int getId() { return id; }

    public String getName() { return name; }
}
