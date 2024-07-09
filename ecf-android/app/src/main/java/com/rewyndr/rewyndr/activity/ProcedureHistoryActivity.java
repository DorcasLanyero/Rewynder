package com.rewyndr.rewyndr.activity;

import android.os.Bundle;

import androidx.appcompat.app.ActionBar;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rewyndr.rewyndr.R;
import com.rewyndr.rewyndr.adapters.ProcedureReportAdapter;
import com.rewyndr.rewyndr.api.resolver.Resolver;
import com.rewyndr.rewyndr.api.resource.ProceduresResource;
import com.rewyndr.rewyndr.model.Procedure;
import com.rewyndr.rewyndr.model.ProcedureResult;
import com.rewyndr.rewyndr.utility.ToastUtility;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class ProcedureHistoryActivity extends BaseActivity {
    final String TAG = "ProcedureHistoryActivity";

    TextView procedureTitle;
    ExpandableListView procedureList;

    TextView loadingText;
    RelativeLayout progressOverlay;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_procedure_history);

        ActionBar titleBar = getSupportActionBar();
        titleBar.setDisplayHomeAsUpEnabled(true);
        titleBar.setDisplayShowTitleEnabled(true);
        setTitle("Procedure History");

        procedureTitle = findViewById(R.id.procedure_history_title);
        procedureList = findViewById(R.id.procedures_list);

        loadingText = findViewById(R.id.loadingText);
        progressOverlay = findViewById(R.id.progress_overlay);

        getProcedure();
    }

    private void showProgressOverlay(String message){
        loadingText.setText(message);
        progressOverlay.setVisibility(VISIBLE);
    }

    private void dismissProgressOverlay(){
        progressOverlay.setVisibility(GONE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void getProcedure(){
        Procedure procedure;
        try {
            procedure = new Procedure(new JSONObject(getIntent().getStringExtra("procedure")));
        }catch(JSONException e){
            Log.e(TAG, "Failed to deserialize procedure from intent: " + e.getMessage());
            return;
        }

        showProgressOverlay("Fetching Procedure results, please wait...");

        ProceduresResource.listExecutions(procedure.getId(), new Resolver() {
            @Override
            public void onSuccess(String data) {
                ArrayList<ProcedureResult> procedures = ProcedureResult.fromProcedureArrayList(Procedure.deserializeProcedures(data));
                getSupportActionBar().setDisplayShowTitleEnabled(true);

                ArrayList<ProcedureResult> completedExecutions = new ArrayList<>();

                for(ProcedureResult r : procedures){
                    if(r.isComplete())
                        completedExecutions.add(r);
                }

                ProcedureReportAdapter adapter = new ProcedureReportAdapter(ProcedureHistoryActivity.this, completedExecutions);

                if(procedures.size() > 0) {
                    procedureTitle.setText(procedures.get(0).getName());
                }

                procedureList.setAdapter(adapter);
                procedureList.setOnChildClickListener(
                        new ExpandableListView.OnChildClickListener() {
                            @Override
                            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                                parent.collapseGroup(groupPosition);
                                Log.d(TAG, "Child clicked!");
                                return false;
                            }
                        });

                stopRefresh();
                dismissProgressOverlay();
            }

            @Override
            public void onError(String data) {
                Log.e(TAG, "Error retrieving procedure: " + data);

                ToastUtility.popShort(ProcedureHistoryActivity.this, "There was an issue loading procedure results, please check your internet connection and try again..");
                stopRefresh();
                dismissProgressOverlay();
            }
        });
    }
}
