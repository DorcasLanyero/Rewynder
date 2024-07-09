package com.rewyndr.rewyndr.activity;

import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import androidx.appcompat.app.ActionBar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;

import com.loopj.android.http.RequestParams;
import com.rewyndr.rewyndr.R;
import com.rewyndr.rewyndr.api.resolver.Resolver;
import com.rewyndr.rewyndr.api.resource.AnnotationsResource;
import com.rewyndr.rewyndr.model.Annotation;
import com.rewyndr.rewyndr.utility.ToastUtility;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AnnotationFormActivity extends BaseActivity {
    private static String TAG = "AnnotationFormActivity";

    private boolean newAnnotation;
    private int annotationId = 0;
    private int stepId = 0;
    private int imageId = 0;
    private int tagId = 0;

    private Annotation annotation;

    // Audio recording
    private ImageButton mRecordButton;
    private MediaRecorder mRecorder;
    private Chronometer mChronometer;
    private boolean isRecording = false;
    private boolean recordingCompleted = false;
    private String mAudioFileName;
    private File mImageFile;

    // Inputs
    private EditText contentInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_annotation_form);
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        contentInput = (EditText) findViewById(R.id.annotation_content_input);

        annotationId = getIntent().getIntExtra("annotationId", 0);
        stepId = getIntent().getIntExtra("executionId", 0);
        imageId = getIntent().getIntExtra("imageId", 0);
        tagId = getIntent().getIntExtra("tagId", 0);

        newAnnotation = annotationId == 0 ? true : false;

        if(!newAnnotation) {
            getAnnotation();
        }

        setTitle(newAnnotation ? "Add Comment" : "Edit Comment");
        actionBar.setDisplayShowTitleEnabled(true);

        Button saveButton = (Button)findViewById(R.id.save_annotation_button);
        saveButton.setText(newAnnotation ? "Create Comment" : "Save Comment");

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveAnnotation();
            }
        });

        // Set up audio recording
        try {
            createAudioFile();
        } catch (IOException e) {
            Log.d(TAG, "Error creating audio file: " + e.getMessage());
        }

        mChronometer = (Chronometer) findViewById(R.id.chronometer);
        mRecordButton = (ImageButton) findViewById(R.id.record);
        mRecordButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!isRecording) {
                    mRecordButton.setImageResource(R.drawable.recorder_pause);
                    Animation anim = AnimationUtils.loadAnimation(AnnotationFormActivity.this, R.anim.recording);
                    mRecordButton.startAnimation(anim);
                    startRecording();
                } else {
                    mRecordButton.setImageResource(R.drawable.recorder_record);
                    mRecordButton.clearAnimation();
                    stopRecording();
                    mRecordButton.setEnabled(false);
                }
                isRecording = !isRecording;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.annotation_form_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.menu_item_save_annotation:
                saveAnnotation();
                break;
        }


        return super.onOptionsItemSelected(item);
    }

    private void createAudioFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String audioFileName = "rewyndr-audio_" + timeStamp + ".3gp";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        mImageFile = new File(storageDir, audioFileName);
        mAudioFileName = mImageFile.getAbsolutePath();
        Log.d(TAG, "Audio file name: " + mAudioFileName);
    }

    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mAudioFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        mChronometer.setBase(SystemClock.elapsedRealtime());
        mChronometer.setText("00:00:00");
        mChronometer
                .setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
                    @Override
                    public void onChronometerTick(Chronometer chronometer) {
                        CharSequence text = chronometer.getText();
                        if (text.length() == 5) {
                            chronometer.setText("00:" + text);
                        } else if (text.length() == 7) {
                            chronometer.setText("0" + text);
                        }
                    }
                });
        mChronometer.start();

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(TAG, "Player 'prepare' failed");
        }

        mRecorder.start();
    }

    private void stopRecording() {
        if (mRecorder!=null){
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
            mChronometer.stop();
        }
        recordingCompleted = true;
    }

    private void getAnnotation() {
        AnnotationsResource.get(annotationId, new Resolver() {
            @Override
            public void onSuccess(String data) {
                try {
                    annotation = new Annotation(new JSONObject(data));
                    contentInput.setText(annotation.getContent());

                    if(annotation.getAnnotateableType() == "Tag") {
                        tagId = annotation.getAnnotateableId();
                    }
                } catch(JSONException e) {
                    Log.e(TAG, "Error parsing procedure JSON: " + e.getMessage());
                }
            }

            @Override
            public void onError(String data) {
                Log.e(TAG, "Error retrieving procedure: " + data);
            }
        });
    }

    private void saveAnnotation() {
        RequestParams data = new RequestParams();

        data.put("content", contentInput.getText());

        String annotateableType = "Image";
        int annotateableId = imageId;

        if(tagId != 0) {
            annotateableType = "Tag";
            annotateableId = tagId;
        }

        data.put("annotateable_type", annotateableType);
        data.put("annotateable_id", annotateableId);

        if(recordingCompleted) {
            try {
                data.put("attachment", mImageFile);
            } catch(FileNotFoundException e) {
                Log.e(TAG, "Audio file not found: " + e.getMessage());
            }
        }

        Resolver saveAnnotationResolver = new Resolver() {
            @Override
            public void onSuccess(String data) {
                try {
                    annotation = new Annotation(new JSONObject(data));
                    ToastUtility.popLong(AnnotationFormActivity.this, "Comment saved");
                    onBackPressed();
                } catch (JSONException e) {
                    Log.e(TAG, "Error deserializing annotation: " + data);
                }

            }

            @Override
            public void onError(String data) {
                Log.e(TAG, "Error saving annotation: " + data);
                ToastUtility.popLong(AnnotationFormActivity.this, errorMessage("Error saving annotation", data));
            }
        };

        if(newAnnotation) {
            AnnotationsResource.create(data, saveAnnotationResolver);
        } else {
            AnnotationsResource.update(annotationId, data, saveAnnotationResolver);
        }

        this.hideKeyboard();
    }
}