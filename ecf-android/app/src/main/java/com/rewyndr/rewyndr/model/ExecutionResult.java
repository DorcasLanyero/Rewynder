package com.rewyndr.rewyndr.model;

import android.text.TextUtils;

import java.io.File;
import java.util.List;

public class ExecutionResult {
    public int executionId;
    public String status;
    public String comment;
    public String imageUrl;
    public File image;

    public ExecutionResult(Step s){
        executionId = s.getId();
        status = s.getExecutionStatus();


        if(s.isComplete()) {
            List<Annotation> stepAnnotations = s.getImage().getAnnotations();

            Annotation lastAnnotation = stepAnnotations.get(stepAnnotations.size() - 1);

            comment = lastAnnotation.getContent();
            imageUrl = lastAnnotation.getAttachmentUrl();
        }
    }

    //TODO: This is not going to be true in later versions; attachment URL can be an audio file too
    public boolean hasImage(){
        return !TextUtils.isEmpty(imageUrl) && !imageUrl.equals("null");
    }
}
