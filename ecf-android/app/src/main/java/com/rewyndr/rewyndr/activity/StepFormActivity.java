package com.rewyndr.rewyndr.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.loopj.android.http.RequestParams;
import com.rewyndr.rewyndr.R;
import com.rewyndr.rewyndr.api.resolver.Resolver;
import com.rewyndr.rewyndr.api.resource.StepsResource;
import com.rewyndr.rewyndr.model.Step;
import com.rewyndr.rewyndr.utility.ImageUtil;
import com.rewyndr.rewyndr.utility.ToastUtility;
import com.rewyndr.rewyndr.view.SquareImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class StepFormActivity extends BaseActivity {
    private static String TAG = "StepFormActivity";
    private int stepId = 0;
    private int procedureId = 0;
    private boolean newStep;
    private Step step;
    private File image;
    private RelativeLayout mProgressDialog;

    // Inputs
    private EditText nameInput;
    private EditText descriptionInput;
    private SquareImageView imageInput;
    private CheckBox requireImageOnPassCheckBox;
    private Button saveImageToGalleryButton;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_step_form);
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        nameInput = findViewById(R.id.step_name_input);
        descriptionInput = findViewById(R.id.step_description_input);
        imageInput = findViewById(R.id.step_image_input);
        saveImageToGalleryButton = findViewById(R.id.save_image_to_gallery_button);
        requireImageOnPassCheckBox = findViewById(R.id.step_require_image_on_pass_input);
        mProgressDialog = findViewById(R.id.saving_dialog);

        stepId = getIntent().getIntExtra("executionId", 0);
        procedureId = getIntent().getIntExtra("procedureId", 0);

        newStep = stepId == 0 ? true : false;

        if(!newStep) getStep();

        setTitle(newStep ? "Add Step" : "Edit Step");
        actionBar.setDisplayShowTitleEnabled(true);

        saveButton = (Button)findViewById(R.id.save_step_button);
        saveButton.setText(newStep ? "Create Step" : "Save Step");
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveStep();
            }
        });

        Drawable dr = getResources().getDrawable(R.drawable.ic_add_a_photo_black_24dp, getTheme());
        InsetDrawable nd = new InsetDrawable(dr, 100);
        imageInput.setImageDrawable(nd);

        imageInput.setOnClickListener(view ->
                EasyImage.openChooserWithDocuments(StepFormActivity.this, "Choose or capture an image", 0));

        saveImageToGalleryButton.setOnClickListener(view ->
                SaveImageToGallery());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.step_form_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.menu_item_save_step:
                saveStep();
                break;
        }


        return super.onOptionsItemSelected(item);
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
                    image = images.get(0);

                    if(image.exists()) {
                        if(source == EasyImage.ImageSource.CAMERA_IMAGE && image != null){
                            ImageUtil.applyRotationIfNeeded(image);
                        }

                        Bitmap bm = BitmapFactory.decodeFile(image.getAbsolutePath());
                        imageInput.setImageBitmap(bm);

                        saveImageToGalleryButton.setVisibility(VISIBLE);
                    }
                }
            }
        });
    }

    private void SaveImageToGallery(){
        if(image == null)
            return;

        OutputStream fOut;
        String strDirectory = Environment.getExternalStorageDirectory().toString();

        File f = new File(strDirectory, image.getName());
        Bitmap bm = BitmapFactory.decodeFile(image.getAbsolutePath());
        try {
            fOut = new FileOutputStream(f);

            bm.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
            fOut.flush();
            fOut.close();

            MediaStore.Images.Media.insertImage(getContentResolver(),
                    f.getAbsolutePath(), f.getName(), f.getName());
        } catch (Exception e) {
            ToastUtility.popShort(this, "Failed to save image");
            e.printStackTrace();
            return;
        }

        ToastUtility.popShort(this, "Image saved!");
    }

    private void getStep() {
        StepsResource.get(stepId, new Resolver() {
            @Override
            public void onSuccess(String data) {
                try {
                    step = new Step(new JSONObject(data));

                    nameInput.setText(step.getName());
                    descriptionInput.setText(step.getDescription());
                    requireImageOnPassCheckBox.setChecked(step.getRequireImageOnPass());

                    Glide.with(StepFormActivity.this).load(step.getImageUrl()).into(imageInput);
                } catch(JSONException e) {
                    Log.e(TAG, "Error parsing step JSON: " + e.getMessage());
                }
            }

            @Override
            public void onError(String data) {
                Log.e(TAG, "Error retrieving step: " + data);
            }
        });
    }

    private void disableSaveButton() {
        saveButton.setEnabled(false);
        saveButton.setText(R.string.state_saving_step);
    }

    private void enableSaveButton() {
        saveButton.setEnabled(true);
        saveButton.setText(R.string.action_save_step);
    }

    private void saveStep() {
        RequestParams data = new RequestParams();

        if(newStep) data.put("procedure_id", procedureId);
        data.put("name", nameInput.getText());
        data.put("description", descriptionInput.getText());
        data.put("require_image_on_pass", requireImageOnPassCheckBox.isChecked());

        if(image != null) {
            try {
                data.put("image", image);
            } catch (FileNotFoundException e) {
                Log.e(TAG, "Image file not found: " + e.getMessage());
                ToastUtility.popShort(StepFormActivity.this, "You must provide an image.");
                return;
            }
        } else if(newStep) {
            ToastUtility.popShort(StepFormActivity.this, "You must provide an image.");
            return;
        }

        Resolver saveStepResolver = new Resolver() {
            @Override
            public void onSuccess(String data) {
                try {
                    step = new Step(new JSONObject(data));
                    mProgressDialog.setVisibility(GONE);
                    ToastUtility.popLong(com.rewyndr.rewyndr.activity.StepFormActivity.this, "Step saved");
                    onBackPressed();
                } catch (JSONException e) {
                    Log.e(TAG, "Error deserializing step: " + data);
                }

            }

            @Override
            public void onError(String data) {
                enableSaveButton();
                mProgressDialog.setVisibility(GONE);
                Log.e(TAG, "Error saving step: " + data);
                ToastUtility.popLong(com.rewyndr.rewyndr.activity.StepFormActivity.this, errorMessage("Error saving step", data));
            }
        };

        disableSaveButton();

        mProgressDialog.setVisibility(VISIBLE);

        if(newStep) {
            StepsResource.create(data, saveStepResolver);
        } else {
            StepsResource.update(stepId, data, saveStepResolver);
        }

        this.hideKeyboard();
    }
}
