package com.rewyndr.rewyndr.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rewyndr.rewyndr.adapters.StepCardAdapter;
import com.rewyndr.rewyndr.model.Procedure;
import com.rewyndr.rewyndr.utility.*;

import com.loopj.android.http.RequestParams;
import com.rewyndr.rewyndr.R;
import com.rewyndr.rewyndr.api.resolver.Resolver;
import com.rewyndr.rewyndr.api.resource.ProceduresResource;
import com.rewyndr.rewyndr.api.resource.StepsResource;
import com.rewyndr.rewyndr.utility.ToastUtility;
import com.woxthebox.draglistview.DragListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.SoftReference;
import java.util.ArrayList;

import static android.view.View.GONE;

public class ProcedureActivity extends BaseActivity {
    private static String TAG = "ProcedureActivity";
    private int procedureId = 0;
    private Procedure procedure;
    private DragListView stepList;

    private Button dryRunButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_procedure);

        super.onCreate(savedInstanceState);

        ActionBar supportActionBar = getSupportActionBar();
        if(supportActionBar != null)
            supportActionBar.setDisplayHomeAsUpEnabled(true);

        refreshLayout().setOnRefreshListener(this::getProcedure);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);

        stepList = findViewById(R.id.procedure_steps_list);
        stepList.setLayoutManager(layoutManager);

        StepCardAdapter adapter = new StepCardAdapter(this, new ArrayList<>(), R.layout.card_step, R.id.card_step_image, true);
        stepList.setAdapter(adapter, true);

        dryRunButton = findViewById(R.id.start_dry_run);
        dryRunButton.setOnClickListener(v -> startOperatorDryRun());

        initProcedure();

    }

    @Override
    protected void onResume(){
        super.onResume();

        getProcedure();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.procedure_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.action_add_step:
                Intent addStepIntent = new Intent(ProcedureActivity.this, StepFormActivity.class);
                addStepIntent.putExtra("procedureId", procedure.getId());
                startActivity(addStepIntent);
                break;
            case R.id.action_edit_procedure:
                Intent editProcedureIntent = new Intent(ProcedureActivity.this, ProcedureFormActivity.class);
                editProcedureIntent.putExtra("procedureId", procedure.getId());
                startActivity(editProcedureIntent);
                break;
            case R.id.action_delete_procedure:
                AlertDialog.Builder builder = new AlertDialog.Builder(ProcedureActivity.this);
                builder.setMessage(R.string.procedure_delete_dialog_message);
                builder.setTitle(R.string.procedure_delete_dialog_title);

                builder.setPositiveButton(R.string.ok, (dialog, id) ->
                    ProceduresResource.delete(procedure.getId(), new Resolver() {
                        @Override
                        public void onSuccess(String data) {
                            ToastUtility.popLong(ProcedureActivity.this, "Procedure deleted");
                            onBackPressed();
                        }

                        @Override
                        public void onError(String data) {
                            ToastUtility.popLong(ProcedureActivity.this, "Could not delete procedure");
                        }
                    }));

                builder.setNegativeButton(R.string.cancel, (dialog, id) -> dialog.cancel());

                builder.create().show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initProcedure(){
        Bundle extra = getIntent().getExtras();
        if(extra == null)
            return;

        String serializedProcedure = extra.getString("procedure");

        try {
            JSONObject json = new JSONObject(serializedProcedure);
            procedure = new Procedure(json);
            procedureId = procedure.getId();
        } catch(JSONException e){
            Log.e(TAG, "Failed to initialize procedure");
        }
    }

    private void getProcedure() {
        if(procedureId == 0) {
            procedureId = getIntent().getIntExtra("procedureId", 0);
        }

        ProceduresResource.get(procedureId, new Resolver() {
            @Override
            public void onSuccess(String data) {
                try {
                    procedure = new Procedure(new JSONObject(data));
                    ActionBar supportActionBar = getSupportActionBar();
                    if(supportActionBar != null)
                        supportActionBar.setDisplayShowTitleEnabled(true);
                    setTitle(procedure.getName());

                    boolean hasDescription = !procedure.getDescription().isEmpty();

                    TextView statusText = findViewById(R.id.procedure_status);
                    String status = procedure.getStatus();
                    statusText.setText(StringUtilsKt.capitalize(status));

                    if(hasDescription) {
                        TextView description = findViewById(R.id.procedure_description);
                        LinearLayout descriptionContainer = findViewById(R.id.procedure_description_container);
                        description.setText(procedure.getDescription());
                        descriptionContainer.setVisibility(View.VISIBLE);
                    }

                    stepList.setDragListListener(new DragListView.DragListListenerAdapter() {

                        int scrollPosition;

                        @Override
                        public void onItemDragStarted(int position) {
                            refreshLayout().setEnabled(false);
                        }

                        @Override
                        public void onItemDragging(int itemPosition, float x, float y) {
                            refreshLayout().clearDisappearingChildren();
                            stepList.scrollBy(0, scrollPosition + 3);
                            stepList.canScrollVertically(-1);
                        }


                        @Override
                        public void onItemDragEnded(int fromPosition, int toPosition) {
                            refreshLayout().setEnabled(true);
                            scrollPosition = stepList.getVerticalScrollbarPosition();
                            stepList.scrollTo(0, scrollPosition);

                            if (fromPosition != toPosition) {
                                long id = stepList.getAdapter().getUniqueItemId(toPosition);
                                Log.d(TAG, "to: " + toPosition);
                                Log.d(TAG, "id: " + stepList.getAdapter().getUniqueItemId(fromPosition));
                                StepsResource.update((int)id, new RequestParams("position", toPosition + 1), new Resolver() {
                                    @Override
                                    public void onSuccess(String data) {
                                        ToastUtility.popShort(ProcedureActivity.this, "Step order updated");
                                    }

                                    @Override
                                    public void onError(String data) {
                                        ToastUtility.popShort(ProcedureActivity.this,"Error updating step order");
                                        Log.d(TAG, "Error updating step order: " + data);
                                    }
                                });
                            }
                        }


                    });

                    StepCardAdapter adapter = new StepCardAdapter(ProcedureActivity.this, procedure.getSteps(), R.layout.card_step, R.id.card_step_handle, true);
                    stepList.setAdapter(adapter, true);
                    stepList.setCanDragHorizontally(false);

                    if(!procedure.getSteps().isEmpty()) {
                        TextView noProceduresText = findViewById(R.id.procedure_no_steps);
                        noProceduresText.setVisibility(GONE);

                        TextView reorderInstructions = findViewById(R.id.procedure_reorder_instructions);
                        reorderInstructions.setVisibility(View.VISIBLE);
                    } else {
                        // Otherwise, hide the step list
                        stepList.setVisibility(GONE);
                        dryRunButton.setVisibility(GONE);
                    }

                    stopRefresh();
                } catch(JSONException e) {
                    Log.e(TAG, "Error parsing procedure JSON: " + e.getMessage());
                }
            }

            @Override
            public void onError(String data) {
                Log.e(TAG, "Error retrieving procedure: " + data);
                stopRefresh();
            }
        });
    }

    private void startOperatorDryRun(){
        Intent intent = new Intent(ProcedureActivity.this, ExecutionActivity.class);

        intent.putExtra("procedure", procedure.serialize());
        intent.putExtra("isDryRun", true);
        startActivity(intent);
    }
}

