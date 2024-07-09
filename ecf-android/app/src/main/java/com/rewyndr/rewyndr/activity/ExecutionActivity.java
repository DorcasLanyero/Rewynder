package com.rewyndr.rewyndr.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.gson.Gson;
import com.loopj.android.http.RequestParams;
import com.rewyndr.rewyndr.R;
import com.rewyndr.rewyndr.adapters.AnnotationCardAdapter;
import com.rewyndr.rewyndr.api.resolver.Resolver;
import com.rewyndr.rewyndr.api.resource.ProceduresResource;
import com.rewyndr.rewyndr.api.resource.StepsResource;
import com.rewyndr.rewyndr.databinding.ActivityExecutionBinding;
import com.rewyndr.rewyndr.enums.RewyndrImageState;
import com.rewyndr.rewyndr.interfaces.IRewyndrImageListener;
import com.rewyndr.rewyndr.interfaces.IRewyndrImageObserver;
import com.rewyndr.rewyndr.interfaces.IRewyndrImageProvider;
import com.rewyndr.rewyndr.model.ExecutionResult;
import com.rewyndr.rewyndr.model.Image;
import com.rewyndr.rewyndr.model.Procedure;
import com.rewyndr.rewyndr.model.ProcedureResult;
import com.rewyndr.rewyndr.model.Step;
import com.rewyndr.rewyndr.model.Tag;
import com.rewyndr.rewyndr.utility.ImageUtil;
import com.rewyndr.rewyndr.utility.ToastUtility;
import com.rewyndr.rewyndr.view.RewyndrImageView;

import org.json.JSONException;
import org.json.JSONObject;

import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class ExecutionActivity extends BaseActivity implements IRewyndrImageObserver, IRewyndrImageProvider {
    public static String TAG = "ExecutionActivity";

    private ProcedureResult procedureResult;
    private List<Step> steps;
    private int stepNumber;
    private String title;

    private ActivityExecutionBinding binding;

    private RewyndrImageView image;

    private AnnotationCardAdapter annotationAdapter;

    boolean stepPassed = false;

    private Button.OnClickListener passClickListener;
    private Button.OnClickListener failClickListener;

    private Procedure execution;

    private boolean isDryRun;
    private boolean zoomedImageVisible;

    private final ArrayList<IRewyndrImageListener> imageListeners = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityExecutionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();

        isDryRun = intent.getBooleanExtra("isDryRun", false);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
        }

        initializeViews();

        initializeResultData();
    }

    private void initializeResultData(){
        Intent intent = getIntent();

        Gson gson = new Gson();
        String procedureJSON = intent.getStringExtra("procedure");
        Procedure p = gson.fromJson(procedureJSON, Procedure.class);
        showProgressOverlay("Beginning procedure, please wait...");

        Resolver resolver = new Resolver(){
            @Override
            public void onSuccess(String data){
                try {
                    JSONObject json = new JSONObject(data);
                    execution = new Procedure(json);

                    procedureResult = new ProcedureResult(execution);
                    steps = execution.getSteps();

                    for(int i = 0; i < steps.size(); i++){
                        procedureResult.setExecutionIdForStep(i, steps.get(i).getId());
                    }

                    initializeStep();
                    updateStepData();
                }catch(JSONException e){
                    Log.e(TAG, "Could not deserialize execution: " + e.getMessage());
                }finally{
                    dismissProgressOverlay();
                }
            }

            @Override
            public void onError(String data){
                ToastUtility.popShort(ExecutionActivity.this, "Sorry, there was an error loading the procedure. Please try again later");

                if(isDryRun){
                    Log.e(TAG, "Failed to begin dry run: " + data);
                }else{
                    Log.e(TAG, "Failed to begin execution: " + data);
                }

                initializeStep();
                updateStepData();
                dismissProgressOverlay();
            }
        };

        if(isDryRun) {
            ProceduresResource.get(p.getId(), resolver);
        }else {
            ProceduresResource.beginExecution(p.getId(), resolver);
        }
    }

    private void showProgressOverlay(String message){
        binding.loadingText.setText(message);
        disablePassFailButtons();
        hideStepUI();
        binding.progressOverlay.setVisibility(VISIBLE);
    }

    private void dismissProgressOverlay(){
        enablePassFailButtons();
        showStepUI();
        binding.progressOverlay.setVisibility(GONE);
    }

    private void hideStepUI(){
        binding.passButton.setVisibility(GONE);
        binding.failButton.setVisibility(GONE);
        binding.stepContentContainer.setVisibility(GONE);
        binding.imageLayout.setVisibility(GONE);
    }

    private void showStepUI(){
        binding.passButton.setVisibility(VISIBLE);
        binding.failButton.setVisibility(VISIBLE);
        binding.stepContentContainer.setVisibility(VISIBLE);
        binding.imageLayout.setVisibility(VISIBLE);
    }

    private void initializeViews(){
        image = findViewById(R.id.step_image);

        image.registerObserver(this);

        View.OnClickListener cameraOpeningClickListener =  (View v) ->
                EasyImage.openCameraForImage(ExecutionActivity.this, 0);

        binding.addPhotoLayout.setOnClickListener(cameraOpeningClickListener);
        binding.resultImage.setOnClickListener(cameraOpeningClickListener);

        passClickListener = (View v) -> doPass();
        failClickListener = (View v) -> doFail();

        binding.zoomIcon.setOnClickListener(v -> setShowZoomedImage(true));
    }

    private void initializeStep(){
        loadStepImage();

        enablePassFailButtons();

        binding.resultOverlay.setVisibility(GONE);
    }

    private void loadStepImage(){
        Image img = steps.get(stepNumber).getImage();

        image.setImage(img);

        annotationAdapter = new AnnotationCardAdapter(ExecutionActivity.this, getCurrentUser(), img != null ? img.getAnnotations() : null);
        binding.stepAnnotationsList.setAdapter(annotationAdapter);
        updateAnnotations(null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home)
            onBackPressed();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
        if(zoomedImageVisible){
            binding.zoomImageFragment.setVisibility(GONE);
            zoomedImageVisible = false;
            return;
        }

        if(resultOverlayIsVisible()){
            enablePassFailButtons();
            binding.resultOverlay.setVisibility(GONE);
            return;
        }

        if(stepNumber != 0){
            stepNumber--;
            updateStepData();
            initializeStep();
        }else {
            super.onBackPressed();
        }
    }

    private boolean resultOverlayIsVisible(){
        return binding.resultOverlay.getVisibility() == VISIBLE;
    }

    private void updateStepData(){
        title = "Step " + (stepNumber+1) + ": " + steps.get(stepNumber).getName();
        setTitle(title);
        binding.stepInstructions.setText(steps.get(stepNumber).getDescription());

        binding.zoomImageFragment.setVisibility(GONE);
        zoomedImageVisible = false;

        updateStepResults();
    }

    private void updateStepResults(){
        binding.resultNotes.setText(procedureResult.getTextForStep(stepNumber));
        File img = procedureResult.getImageForStep(stepNumber);

        if (img != null) {
            showStepResultImage(img);
        } else {
            showAddStepResultImageLayout();
        }
    }

    private void showStepResultImage(File image){
        Bitmap bm = BitmapFactory.decodeFile(image.getAbsolutePath());
        binding.resultImage.setImageBitmap(bm);
        binding.resultImageLayout.setVisibility(VISIBLE);
        binding.addPhotoLayout.setVisibility(GONE);
        for(IRewyndrImageListener listener : imageListeners){
            listener.setImageBitmap(bm);
        }
        //resultImage.setRotation(90);
    }

    private void showAddStepResultImageLayout(){
        binding.resultImageLayout.setVisibility(GONE);
        binding.addPhotoLayout.setVisibility(VISIBLE);
    }

    private void doPass(){
        stepPassed = true;
        if(steps.get(stepNumber).getRequireImageOnPass()){
            EasyImage.openCameraForImage(ExecutionActivity.this, 0);
        }else{
            showCommentBox();
        }

    }

    private void doFail(){
        stepPassed = false;
        EasyImage.openCameraForImage(ExecutionActivity.this, 0);
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
            public void onImagesPicked(@NonNull List<File> images, EasyImage.ImageSource source, int type) {
                if(images.size() > 0) {
                    File pickedImage = images.get(0);

                    if(source == EasyImage.ImageSource.CAMERA_IMAGE && pickedImage != null){
                        ImageUtil.applyRotationIfNeeded(pickedImage);
                    }

                    if(pickedImage != null && pickedImage.exists()) {
                        showStepResultImage(pickedImage);
                        procedureResult.setImageForStep(stepNumber, pickedImage);
                    }
                }
                showCommentBox();
            }
        });
    }

    private void showCommentBox(){
        disablePassFailButtons();

        colorAndSetResultText();
        binding.resultOverlay.setVisibility(VISIBLE);

        binding.stepContinueText.setOnClickListener((View v) -> {

            if(commentRequiredAndNotSupplied()){
                return;
            }
            procedureResult.setStatusForStep(stepNumber, stepPassed ? "pass" : "fail");
            procedureResult.setTextForStep(stepNumber, binding.resultNotes.getText().toString());

            binding.resultOverlay.setVisibility(GONE);
            binding.resultNotes.setText("");

            hideKeyboard();

            saveResultAndContinue();
        });
    }

    private boolean commentRequiredAndNotSupplied(){
        String resultDescription = binding.resultNotes.getText().toString();
        boolean needsDescriptionText = !stepPassed && resultDescription.isEmpty();
        if(needsDescriptionText){
            ToastUtility.popShort(ExecutionActivity.this, "Please provide a description for this step's result.");
            return true;
        }
        return false;
    }

    private void enablePassFailButtons(){
        binding.passButton.setOnClickListener(passClickListener);
        binding.failButton.setOnClickListener(failClickListener);
    }

    private void disablePassFailButtons(){
        binding.passButton.setOnClickListener(null);
        binding.failButton.setOnClickListener(null);
    }

    private void colorAndSetResultText(){
        int color;
        if(stepPassed){
            binding.resultText.setText(R.string.step_pass_result);
            color = R.color.passGreen;
        }else{
            binding.resultText.setText(R.string.step_fail_result);
            color = R.color.failRed;
        }
        binding.resultText.setTextColor(getResources().getColor(color, getTheme()));
        binding.stepContinueText.setTextColor(getResources().getColor(color, getTheme()));
    }

    private void saveResultAndContinue(){
        if(isDryRun){
            enablePassFailButtons();
            navigateToNextStep();
            return;
        }

        ExecutionResult result = procedureResult.getStepResult(stepNumber);
        RequestParams data = new RequestParams();

        data.put("status", result.status);
        data.put("comment", result.comment);

        try {
            if(result.image != null) {
                data.put("image", result.image);
            }
        }catch(FileNotFoundException e){
            ToastUtility.popShort(ExecutionActivity.this, "Failed to find image for step result");
            Log.e(TAG, "Failed to find image for execution ID: " + result.executionId);
        }

        showProgressOverlay("Saving step result...");

        StepsResource.saveExecutionResult(procedureResult.getExecutionIdForStep(stepNumber), data, new Resolver(){
            @Override
            public void onSuccess(String data){
                navigateToNextStep();
                dismissProgressOverlay();
            }

            @Override
            public void onError(String data){
                //TODO: cache step post here
                ToastUtility.popShort(ExecutionActivity.this, "There was a problem saving your execution's status for this step, please check your network connection");
                Log.e(TAG, "Failed to save step status. Step ID: " + procedureResult.getExecutionIdForStep(stepNumber));
                Log.e(TAG, "Data:" + data);
                enablePassFailButtons();
                dismissProgressOverlay();
            }
        });
    }

    private void navigateToNextStep(){
        stepNumber++;
        if(stepNumber == steps.size()){
            showFinishExecutionDialog();
        }else{
            updateStepData();
            initializeStep();
        }
    }

    private void showFinishExecutionDialog(){
        new AlertDialog.Builder(ExecutionActivity.this, R.style.ExecutionDialogTheme)
                .setMessage(R.string.procedure_completed)
                .setPositiveButton(R.string.procedure_submit, (DialogInterface dialogInterface, int i) -> {
                    ToastUtility.popShort(ExecutionActivity.this, "Procedure saved.");
                    navigateToProcedure();
                })
                .setNegativeButton(R.string.procedure_delete, (DialogInterface dialogInterface, int i) -> {
                    if(!isDryRun){
                        deleteResults();
                        deleteExecutionInstance();
                    }
                    ToastUtility.popShort(ExecutionActivity.this, "Procedure deleted.");
                    navigateToProcedure();
                })
                .show();
    }

    private void deleteResults(){
        if(isDryRun)
            return;

        for(Step s : steps){
            StepsResource.delete(s.getId(), new Resolver() {
                @Override
                public void onSuccess(String data){
                    Log.i(TAG, "Step deleted: " + s.getId());
                }

                @Override
                public void onError(String data){
                    Log.e(TAG, "Failed to delete step: " + data);
                }
            });
        }
    }

    private void deleteExecutionInstance(){
        if(isDryRun)
            return;

        ProceduresResource.delete(execution.getId(), new Resolver(){
            @Override
            public void onSuccess(String data){
                Log.i(TAG, "Execution instance deleted: " + execution.getId());
            }

            @Override
            public void onError(String data){
                Log.e(TAG, "Execution deletion failed:  " + data);
            }
        });
    }

    private void navigateToProcedure(){
        super.onBackPressed();
    }

    public void imageChanged(Bitmap newBitmap){
        for (IRewyndrImageListener listener : imageListeners) {
            listener.setImageBitmap(newBitmap);
        }
    }
    public void tagBoundsUpdated(List<Point> list){}

    public void stateUpdate(RewyndrImageState state){

    }

    public void tagUpdate(Tag t){
        updateAnnotations(t);
    }

    private void updateAnnotations(Tag newTag){
        if(newTag != null){
            setTitle(newTag.getName());
        }else{
            setTitle(title);
        }

        annotationAdapter.setTag(newTag);
        annotationAdapter.notifyDataSetChanged();
        binding.annotationListHeading.setText(String.format(getResources().getString(R.string.comments_count), annotationAdapter.getCount()));
    }

    @Override
    public void registerListener(@NonNull IRewyndrImageListener listener){
        imageListeners.add(listener);
    }

    @Override
    public void setShowZoomedImage(boolean showImage){
        zoomedImageVisible = showImage;
        binding.zoomImageFragment.setVisibility(showImage ? VISIBLE : GONE);
    }
}
