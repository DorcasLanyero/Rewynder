package com.rewyndr.rewyndr.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.amulyakhare.textdrawable.TextDrawable;
import com.bumptech.glide.Glide;
import com.loopj.android.http.RequestParams;
import com.rewyndr.rewyndr.R;
import com.rewyndr.rewyndr.api.resolver.Resolver;
import com.rewyndr.rewyndr.api.resource.CommunitiesResource;
import com.rewyndr.rewyndr.api.resource.LocationsResource;
import com.rewyndr.rewyndr.model.Community;
import com.rewyndr.rewyndr.model.Location;
import com.rewyndr.rewyndr.utility.ImageUtil;
import com.rewyndr.rewyndr.utility.ToastUtility;
import com.rewyndr.rewyndr.view.SquareImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

public class CommunityFormActivity extends BaseActivity {
    private static String TAG = "CommunityFormActivity";
    private int communityId = 0;
    private boolean newCommunity;
    private Community community;
    private ArrayList<Location> locations;
    private File featuredImage;

    // Inputs
    private EditText nameInput;
    private EditText descriptionInput;
    private EditText machineNumberInput;
    private Spinner locationSpinner;
    private SquareImageView featuredImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_community_form);
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        nameInput = (EditText) findViewById(R.id.community_name_input);
        machineNumberInput = (EditText) findViewById(R.id.community_machine_number_input);
        descriptionInput = (EditText) findViewById(R.id.community_description_input);
        locationSpinner = (Spinner)findViewById(R.id.community_location_input);
        featuredImageView = findViewById(R.id.featured_image_input);

        communityId = getIntent().getIntExtra("communityId", 0);
        newCommunity = communityId == 0 ? true : false;

        // If this is a new community, we need only retrieve locations, as there is no existing
        // community data to retrieve
        if(newCommunity) {
            getLocations();
        } else {
            getCommunity();
        }

        setTitle(newCommunity ? "Add Community" : "Edit Community");
        actionBar.setDisplayShowTitleEnabled(true);

        Button saveButton = (Button)findViewById(R.id.save_community_button);
        saveButton.setText(newCommunity ? "Create Community" : "Save Community");

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveCommunity();
            }
        });

        Drawable dr = getResources().getDrawable(R.drawable.ic_add_a_photo_black_24dp, getTheme());
        InsetDrawable nd = new InsetDrawable(dr, 100);
        featuredImageView.setImageDrawable(nd);

        featuredImageView.setOnClickListener(v -> {
                EasyImage.openChooserWithDocuments(CommunityFormActivity.this, "Choose or capture an image", 0);
            });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.community_form_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.menu_item_save_community:
                saveCommunity();
                break;
        }


        return super.onOptionsItemSelected(item);
    }

    private void getCommunity() {
        CommunitiesResource.get(communityId, new Resolver() {
            @Override
            public void onSuccess(String data) {
                try {
                    community = new Community(new JSONObject(data));

                    nameInput.setText(community.getName());
                    machineNumberInput.setText(community.getMachineNumber());
                    descriptionInput.setText(community.getDescription());

                    if(community.hasFeaturedImage()){
                        TextDrawable td = TextDrawable.builder().buildRect(community.getName().substring(0,1), R.color.brandLightBackground);
                        Glide.with(CommunityFormActivity.this).load(community.getImageUrl())
                                .placeholder(td).into(featuredImageView);


                    }

                    getLocations();
                } catch(JSONException e) {
                    Log.e(TAG, "Error parsing community JSON: " + e.getMessage());
                }
            }

            @Override
            public void onError(String data) {
                Log.e(TAG, "Error retrieving community: " + data);
            }
        });
    }

    private void getLocations() {
         // Fetch locations, populate spinner, and set selected location
        LocationsResource.list(new Resolver() {
            @Override
            public void onSuccess(String data) {
                int selectedLocation = -1;
                locations = Location.deserializeLocations(data);
                String[] locationNames = new String[locations.size()];

                for(int i = 0; i < locations.size(); i++) {
                    Location location = locations.get(i);
                    locationNames[i] = location.getName();

                    // Find the position of the selected location if the community has
                    // already been persisted
                    if(!newCommunity && location.getId() == community.getLocationId()) {
                        selectedLocation = i;
                    }
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(CommunityFormActivity.this, android.R.layout.simple_spinner_item);
                adapter.add("No Location");
                adapter.addAll(locationNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                locationSpinner.setAdapter(adapter);

                if(selectedLocation != -1) {
                    locationSpinner.setSelection(selectedLocation + 1);
                }
            }

            @Override
            public void onError(String data) {
                Log.e(TAG, "Error retrieving locations: " + data);
            }
        });
    }

    private void saveCommunity() {
        RequestParams data = new RequestParams();

        data.put("name", nameInput.getText());
        data.put("machine_number", machineNumberInput.getText());
        data.put("description", descriptionInput.getText());

        int selectedLocation = locationSpinner.getSelectedItemPosition();
        if(selectedLocation == 0) {
            data.put("location_id", "");
        } else {
            data.put("location_id", locations.get(selectedLocation - 1).getId());
        }

        if(featuredImage != null) {
            try {
                data.put("featured_image", featuredImage);
            } catch (FileNotFoundException e) {
                Log.e(TAG, "Image file not found: " + e.getMessage());
                ToastUtility.popShort(CommunityFormActivity.this, "There was an issue saving your featured image. Please exit the Community creation screen and try again.");
                return;
            }
        }

        Resolver saveCommunityResolver = new Resolver() {
            @Override
            public void onSuccess(String data) {
                try {
                    community = new Community(new JSONObject(data));
                    ToastUtility.popLong(CommunityFormActivity.this, "Community saved");
                    onBackPressed();
                } catch (JSONException e) {
                    Log.e(TAG, "Error deserializing community: " + data);
                }

            }

            @Override
            public void onError(String data) {
                Log.e(TAG, "Error saving community: " + data);
                ToastUtility.popLong(CommunityFormActivity.this, errorMessage("Error saving community", data));
            }
        };

        if(newCommunity) {
            CommunitiesResource.create(data, saveCommunityResolver);
        } else {
            CommunitiesResource.update(communityId, data, saveCommunityResolver);
        }

        this.hideKeyboard();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        EasyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {
                Log.e(TAG, "Error picking image: " + e.getMessage());
            }

            @Override
            public void onImagesPicked(List<File> images, EasyImage.ImageSource source, int type) {
                if(images.size() > 0) {
                    featuredImage = images.get(0);

                    if(featuredImage.exists()) {
                        if(source == EasyImage.ImageSource.CAMERA_IMAGE && featuredImage != null){
                            ImageUtil.applyRotationIfNeeded(featuredImage);
                        }

                        Bitmap bm = BitmapFactory.decodeFile(featuredImage.getAbsolutePath());
                        featuredImageView.setImageBitmap(bm);
                    }
                }
            }
        });
    }
}
