package com.rewyndr.rewyndr.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.rewyndr.rewyndr.R;
import com.rewyndr.rewyndr.activity.ResultImageActivity;
import com.rewyndr.rewyndr.model.Annotation;
import com.rewyndr.rewyndr.model.ProcedureResult;
import com.rewyndr.rewyndr.model.ExecutionResult;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class ProcedureReportAdapter extends BaseExpandableListAdapter {
    private Activity context;
    private ArrayList<ProcedureResult> procedureResults;

    public ProcedureReportAdapter(Activity a, ArrayList<ProcedureResult> results){
        context = a;
        this.procedureResults = results;
    }

    @Override
    public int getGroupCount() {
        return procedureResults.size();
    }

    @Override
    public int getChildrenCount(int i) {
        return procedureResults.get(i).getStepResults().size();
    }

    @Override
    public Object getGroup(int i) {
        return procedureResults.get(i);
    }

    @Override
    public Object getChild(int i, int i1) {
        return procedureResults.get(i).getStepResults().get(i1);
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int i1) {
        return i1;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int i, boolean isExpanded, View convertView, ViewGroup viewGroup) {
        ProcedureResult results = procedureResults.get(i);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.completed_procedure_card, null);
        }

        TextView dateTextView = convertView.findViewById(R.id.report_date);
        TextView timeTextView = convertView.findViewById(R.id.report_time);
        TextView usernameTextView = convertView.findViewById(R.id.report_username);
        ImageView failureIndicator = convertView.findViewById(R.id.report_failure_indicator);

        DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE.withZone(ZoneId.systemDefault());
        String datestamp = dateFormatter.format(results.getTimeCompleted());

        DateTimeFormatter timeFormatter = DateTimeFormatter.ISO_LOCAL_TIME.withZone(ZoneId.systemDefault());
        String timestamp = timeFormatter.format(results.getTimeCompleted());

        dateTextView.setText(datestamp);
        timeTextView.setText(timestamp);
        usernameTextView.setText(results.getOperatorUserName());

        if(results.containsFailure()){
            failureIndicator.setVisibility(VISIBLE);
        }else{
            failureIndicator.setVisibility(GONE);
        }

        View groupDivider = convertView.findViewById(R.id.procedure_history_group_divider);

        if(isExpanded){
            groupDivider.setVisibility(GONE);
        }else{
            groupDivider.setVisibility(VISIBLE);
        }

        return convertView;
    }

    @Override
    public View getChildView(int i, int i1, boolean isLastChild, View convertView, ViewGroup viewGroup) {
        ProcedureResult results = procedureResults.get(i);
        ExecutionResult executionResult = results.getStepResult(i1);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.completed_step_card, null);
        }

        TextView stepNumber = convertView.findViewById(R.id.step_number);
        TextView stepDescription = convertView.findViewById(R.id.step_description);
        TextView stepResultTextView = convertView.findViewById(R.id.step_result);
        TextView resultComments = convertView.findViewById(R.id.result_comments);
        ImageView imageIndicator = convertView.findViewById(R.id.image_indicator);

        List<Annotation> stepAnnotations = results.getSteps().get(i1).getImage().getAnnotations();

        Annotation lastAnnotation = stepAnnotations.get(stepAnnotations.size()-1);

        stepNumber.setText("Step " + (i1+1) + ":");
        stepDescription.setText(results.getSteps().get(i1).getName());
        stepResultTextView.setText(executionResult.status.substring(0,1).toUpperCase() + executionResult.status.substring(1));
        resultComments.setText("Comments: " + lastAnnotation.getContent());

        int color = 0;

        if(executionResult.status.equals("pass")){
            color = R.color.passGreen;
        }else{
            color = R.color.failRed;
        }

        stepNumber.setTextColor(ContextCompat.getColor(context, color));
        stepDescription.setTextColor(ContextCompat.getColor(context, color));
        stepResultTextView.setTextColor(ContextCompat.getColor(context, color));


        if(executionResult.hasImage()){
            imageIndicator.setVisibility(VISIBLE);
            setupImageClickListener(imageIndicator, executionResult.executionId);
        }else{
            imageIndicator.setVisibility(GONE);
        }

        View groupDivider = convertView.findViewById(R.id.procedure_history_child_divider);

        if(isLastChild){
            groupDivider.setVisibility(VISIBLE);
        }else{
            groupDivider.setVisibility(GONE);
        }

        return convertView;
    }

    private void setupImageClickListener(ImageView indicator, int stepId){
        indicator.setOnClickListener(new ImageView.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent editStepIntent = new Intent(context, ResultImageActivity.class);
                editStepIntent.putExtra("executionId", stepId);
                context.startActivity(editStepIntent);
            }
        });
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return false;
    }
}
