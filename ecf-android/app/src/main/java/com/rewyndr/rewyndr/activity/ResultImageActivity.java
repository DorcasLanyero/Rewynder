package com.rewyndr.rewyndr.activity;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.rewyndr.rewyndr.R;
import com.rewyndr.rewyndr.api.resolver.Resolver;
import com.rewyndr.rewyndr.api.resource.StepsResource;
import com.rewyndr.rewyndr.enums.RewyndrImageState;
import com.rewyndr.rewyndr.interfaces.IRewyndrImageObserver;
import com.rewyndr.rewyndr.model.Annotation;
import com.rewyndr.rewyndr.model.Image;
import com.rewyndr.rewyndr.model.Step;
import com.rewyndr.rewyndr.model.Tag;
import com.rewyndr.rewyndr.utility.ToastUtility;
import com.rewyndr.rewyndr.view.AnnotationsListview;
import com.rewyndr.rewyndr.view.RewyndrImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class ResultImageActivity extends BaseActivity implements IRewyndrImageObserver {
    public final String TAG = "ResultImageActivity";

    RewyndrImageView imageView;
    AnnotationsListview listview;
    RelativeLayout progressOverlay;
    LinearLayout imageLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_image);

        initializeViews();
    }

    private void initializeViews() {
        imageView = findViewById(R.id.result_image);
        listview = findViewById(R.id.annotations_listview);
        progressOverlay = findViewById(R.id.progress_overlay);
        imageLayout = findViewById(R.id.image_layout);

        imageView.registerObserver(this);

        initializeImage();
    }

    private void initializeImage() {
        int id = getIntent().getIntExtra("executionId", 0);

        StepsResource.get(id, new Resolver() {
            @Override
            public void onSuccess(String data) {
                try {
                    Step step = new Step(new JSONObject(data));
                    Image image = step.getImage();

                    getSupportActionBar().setDisplayShowTitleEnabled(true);
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

                    setTitle(step.getName());

                    String resultImage = image.getAnnotations().get(image.getAnnotations().size() - 1).getAttachmentUrl();

                    imageView.setImage(resultImage);

                    progressOverlay.setVisibility(VISIBLE);

                    List<Annotation> stepAnnotations = image.getAnnotations();
                    ArrayList<Annotation> commentAnnotationList = new ArrayList<>();

                    commentAnnotationList.add(stepAnnotations.get(stepAnnotations.size() - 1));

                    listview.initAdapter(getCurrentUser(), commentAnnotationList);
                } catch(JSONException e) {
                    Log.e(TAG, "Error deserializing step: " + e.getMessage());
                }
            }

            @Override
            public void onError(String data) {
                ToastUtility.popShort(ResultImageActivity.this, "Error retrieving step");
                Log.e(TAG, "Error retrieving step: " + data);
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    public void imageChanged(Bitmap newBitmap){
        progressOverlay.setVisibility(GONE);
        imageLayout.setVisibility(VISIBLE);
        listview.setVisibility(VISIBLE);
    }

    public void stateUpdate(RewyndrImageState state){}
    public void tagUpdate(Tag t) {}
    public void tagBoundsUpdated(List<Point> list) {}
}
