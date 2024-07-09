package com.rewyndr.rewyndr.view;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.rewyndr.rewyndr.R;
import com.rewyndr.rewyndr.adapters.AnnotationCardAdapter;
import com.rewyndr.rewyndr.model.Annotation;
import com.rewyndr.rewyndr.model.Tag;
import com.rewyndr.rewyndr.model.User;

import java.util.ArrayList;

public class AnnotationsListview extends LinearLayout {
    Context context;
    TextView commentCountTextView;
    ListView annotationsList;

    AnnotationCardAdapter adapter;

    public AnnotationsListview(Context context){
        super(context);
        this.context = context;
        init();
    }

    public AnnotationsListview(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }
    public AnnotationsListview(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        init();
    }

    private void init(){
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.annotations_listview, this);

        commentCountTextView = findViewById(R.id.annotation_list_heading);
        annotationsList = findViewById(R.id.step_annotations_list);
    }

    public void initAdapter(User user, ArrayList<Annotation> annotations){
        adapter = new AnnotationCardAdapter(context, user, annotations);
        annotationsList.setAdapter(adapter);
        update(null);
    }

    public void update(Tag t){
        adapter.setTag(t);

        new Handler().post(new Runnable() {
            public void run () {
                adapter.notifyDataSetChanged();
            }
        });

        String comments = String.format(context.getString(R.string.comments_count), adapter.getCount());
        commentCountTextView.setText(comments);
    }
}
