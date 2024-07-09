package com.rewyndr.rewyndr.model;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Community {
    private static String TAG = "Community";

    // Members
    private int id;
    private int locationId = 0;
    private boolean hasFeaturedImage = true;
    private String name;
    private String description;
    private String machineNumber;
    private String locationName;
    private String imageUrl = "";
    private String createdAt;
    private String updatedAt;
    private ArrayList<Procedure> procedures;

    // Constructors
    public Community(JSONObject data) {
        try {
            this.id = data.getInt("id");
            this.name = data.getString("name");
            this.description = data.getString("description");
            this.machineNumber = data.getString("machine_number");
            this.createdAt = data.getString("created_at");
            this.updatedAt = data.getString("updated_at");

            if(!data.isNull("location")) {
                this.locationId = data.getJSONObject("location").getInt("id");
                this.locationName = data.getJSONObject("location").getString("name");
            }

            this.procedures = Procedure.deserializeProcedures(data.getJSONArray("procedures"));

            this.imageUrl = data.getString("featured_image_url");

            if(imageUrl.isEmpty()) {
                for (Procedure procedure : procedures) {
                    String imageUrl = procedure.getImageUrl();
                    if (imageUrl.length() > 0 && !imageUrl.isEmpty()) {
                        this.imageUrl = imageUrl;
                        hasFeaturedImage = false;
                        break;
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error deserializing community: " + e.getMessage());
        }
    }

    public static ArrayList<Community> deserializeCommunities(String json) {
        ArrayList<Community> communities = new ArrayList<>();
        JSONArray a;

        try {
            a = new JSONArray(json);
            for(int i = 0; i < a.length(); i++) {
                communities.add(new Community(a.getJSONObject(i)));
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error deserializing communities: " + e.getMessage());
        }

        return communities;
    }

    // Instance methods
    public String getDescription() { return this.description; }

    public int getId() { return this.id; }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getLocationName() {
        return locationName;
    }

    public int getLocationId() { return locationId; }

    public String getMachineNumber() { return machineNumber; }

    public String getName() {
        return name;
    }

    public ArrayList<Procedure> getProcedures() {
        return procedures;
    }

    public boolean hasLocation() {
        return locationId != 0;
    }

    public boolean hasFeaturedImage(){
        return hasFeaturedImage;
    }
}
