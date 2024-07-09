package com.rewyndr.rewyndr.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.rewyndr.rewyndr.R;
import com.rewyndr.rewyndr.adapters.ProcedureCardAdapter;
import com.rewyndr.rewyndr.api.resolver.Resolver;
import com.rewyndr.rewyndr.api.resource.ProceduresResource;
import com.rewyndr.rewyndr.interfaces.IProcedureCoordinator;
import com.rewyndr.rewyndr.model.Community;
import com.rewyndr.rewyndr.model.Procedure;
import com.rewyndr.rewyndr.view.ExpandableHeightGridView;

import java.util.ArrayList;

public class ProcedureHistoryListActivity extends BaseActivity implements IProcedureCoordinator {
    private static String TAG = "ProcedureHistoryListActivity";
    private int communityId = 0;
    private Community community;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_procedure_history_list);

        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.procedure_history);

        refreshLayout().setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getCommunity();
            }
        });

        getCommunity();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.community_menu, menu);
        return super.onCreateOptionsMenu(menu);
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

    private void getCommunity() {
        if(communityId == 0) {
            communityId = getIntent().getIntExtra("communityId", 0);
        }

        String communityName = getIntent().getStringExtra("communityName");

        getSupportActionBar().setDisplayShowTitleEnabled(true);
        setTitle(communityName);

        ProceduresResource.listPublishedForCommunity(communityId, new Resolver() {
            @Override
            public void onSuccess(String data) {
                ArrayList<Procedure> procedures = Procedure.deserializeProcedures(data);

                ExpandableHeightGridView proceduresList = findViewById(R.id.community_procedures_list);
                proceduresList.setExpanded(true);
                proceduresList.setAdapter(new ProcedureCardAdapter(ProcedureHistoryListActivity.this, ProcedureHistoryListActivity.this, procedures));

                if(!procedures.isEmpty()) {
                    TextView noProceduresText = findViewById(R.id.no_procedure_history);
                    noProceduresText.setVisibility(View.GONE);
                } else {
                    proceduresList.setVisibility(View.GONE);
                }

                stopRefresh();
            }

            @Override
            public void onError(String data) {
                Log.e(TAG, "Error retrieving community: " + data);
                stopRefresh();
            }
        });
    }

    public void navigateToProcedure(Procedure procedure){
        Intent intent = new Intent(this, ProcedureHistoryActivity.class);
        intent.putExtra("procedure", procedure.serialize());
        startActivity(intent);
    }
}
