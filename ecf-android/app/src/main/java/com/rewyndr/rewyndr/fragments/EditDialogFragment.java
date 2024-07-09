package com.rewyndr.rewyndr.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.rewyndr.rewyndr.activity.AnnotationFormActivity;
import com.rewyndr.rewyndr.api.resolver.Resolver;
import com.rewyndr.rewyndr.api.resource.AnnotationsResource;
import com.rewyndr.rewyndr.interfaces.IFragmentDetachListener;
import com.rewyndr.rewyndr.utility.ToastUtility;

public class EditDialogFragment extends DialogFragment {
    IFragmentDetachListener listener;

    public static EditDialogFragment newInstance(int annotationId){
        EditDialogFragment frag = new EditDialogFragment();
        Bundle b = new Bundle();
        b.putInt("annotationId", annotationId);
        frag.setArguments(b);
        return frag;
    }

    public void setListener(IFragmentDetachListener listener){
        this.listener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        String[] items = {"Edit", "Delete", "Cancel"};

        int annotationId = getArguments().getInt("annotationId");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Modify comment?")
                .setItems(items, (v, which) -> {
                    handleItemClicked(which, annotationId);
                });

        return builder.create();
    }

    private void handleItemClicked(int item, int annotationId){
        Activity a = getActivity();
        if(item == 0){
            editAnnotation(annotationId, a);
        }else if(item == 1) {
            deleteAnnotation(annotationId, a);
        }
    }

    private void editAnnotation(int annotationId, Activity a){
        Intent editAnnotationIntent = new Intent(a, AnnotationFormActivity.class);
        editAnnotationIntent.putExtra("annotationId", annotationId);
        startActivity(editAnnotationIntent);
    }

    private void deleteAnnotation(int annotationId, Activity a){
        AnnotationsResource.delete(annotationId, new Resolver(){
            @Override
            public void onSuccess(String data){
                ToastUtility.popLong(a, "Annotation deleted");
            }

            @Override
            public void onError(String data){
                ToastUtility.popLong(a, "Failed to delete annotation");
            }
        });
    }

    @Override
    public void onDetach(){
        if(listener != null){
            listener.onFragmentDetached();
        }
        super.onDetach();
    }
}