package com.rewyndr.rewyndr.activity;

import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.loopj.android.http.RequestParams;
import com.rewyndr.rewyndr.R;
import com.rewyndr.rewyndr.api.resolver.Resolver;
import com.rewyndr.rewyndr.api.resource.ProceduresResource;
import com.rewyndr.rewyndr.model.Procedure;
import com.rewyndr.rewyndr.utility.ToastUtility;

import org.json.JSONException;
import org.json.JSONObject;

public class ProcedureFormActivity extends BaseActivity {
    private static String TAG = "ProcedureActivity";
    private int procedureId = 0;
    private int communityId = 0;
    private boolean newProcedure;
    private Procedure procedure;
    private String[] statuses;

    // Inputs
    private EditText nameInput;
    private EditText descriptionInput;
    private Spinner statusSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_procedure_form);
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        nameInput = (EditText) findViewById(R.id.procedure_name_input);
        descriptionInput = (EditText) findViewById(R.id.procedure_description_input);
        statusSpinner = (Spinner)findViewById(R.id.procedure_status_input);

        procedureId = getIntent().getIntExtra("procedureId", 0);
        communityId = getIntent().getIntExtra("communityId", 0);
        newProcedure = procedureId == 0 ? true : false;

        if(newProcedure) {
            LinearLayout statusSpinnerContainer = (LinearLayout)findViewById(R.id.procedure_status_input_container);
            statusSpinnerContainer.setVisibility(View.GONE);
            getStatuses();
        } else {
            getProcedure();
        }

        setTitle(newProcedure ? "Add Procedure" : "Edit Procedure");
        actionBar.setDisplayShowTitleEnabled(true);

        Button saveButton = (Button)findViewById(R.id.save_procedure_button);
        saveButton.setText(newProcedure ? "Create Procedure" : "Save Procedure");

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveProcedure();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.procedure_form_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.menu_item_save_procedure:
                saveProcedure();
                break;
        }


        return super.onOptionsItemSelected(item);
    }

    private void getProcedure() {
        ProceduresResource.get(procedureId, new Resolver() {
            @Override
            public void onSuccess(String data) {
                try {
                    procedure = new Procedure(new JSONObject(data));

                    nameInput.setText(procedure.getName());
                    descriptionInput.setText(procedure.getDescription());

                    getStatuses();
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

    private void getStatuses() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(ProcedureFormActivity.this, android.R.layout.simple_spinner_item);

        statuses = Procedure.getStatuses();
        int selectedStatus = 0;

        for(int i = 0; i < statuses.length; i++) {
            if(!newProcedure && statuses[i].equals(procedure.getStatus())) {
                selectedStatus = i;
            }
        }

        adapter.addAll(statuses);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(adapter);
        statusSpinner.setSelection(selectedStatus);
    }

    private void saveProcedure() {
        RequestParams data = new RequestParams();

        data.put("name", nameInput.getText());
        data.put("community_id", communityId);
        data.put("description", descriptionInput.getText());
        data.put("status", statuses[statusSpinner.getSelectedItemPosition()]);

        Resolver saveProcedureResolver = new Resolver() {
            @Override
            public void onSuccess(String data) {
                try {
                    procedure = new Procedure(new JSONObject(data));
                    ToastUtility.popLong(ProcedureFormActivity.this, "Procedure saved");
                    onBackPressed();
                } catch (JSONException e) {
                    Log.e(TAG, "Error deserializing procedure: " + data);
                }

            }

            @Override
            public void onError(String data) {
                Log.e(TAG, "Error saving procedure: " + data);
                ToastUtility.popLong(ProcedureFormActivity.this, errorMessage("Error saving procedure", data));
            }
        };

        if(newProcedure) {
            ProceduresResource.create(data, saveProcedureResolver);
        } else {
            ProceduresResource.update(procedureId, data, saveProcedureResolver);
        }

        this.hideKeyboard();
    }
}

