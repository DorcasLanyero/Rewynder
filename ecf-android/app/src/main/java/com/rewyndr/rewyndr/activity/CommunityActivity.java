package com.rewyndr.rewyndr.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;
import com.rewyndr.rewyndr.R;
import com.rewyndr.rewyndr.adapters.DraggableProcedureCardAdapter;
import com.rewyndr.rewyndr.api.resolver.Resolver;
import com.rewyndr.rewyndr.api.resource.CommunitiesResource;
import com.rewyndr.rewyndr.api.resource.ProceduresResource;
import com.rewyndr.rewyndr.interfaces.IProcedureCoordinator;
import com.rewyndr.rewyndr.model.Community;
import com.rewyndr.rewyndr.model.Procedure;
import com.rewyndr.rewyndr.utility.ToastUtility;
import com.woxthebox.draglistview.DragListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class CommunityActivity extends BaseActivity implements IProcedureCoordinator {
    private static String TAG = "CommunityActivity";
    private int communityId = 0;
    private Community community;
    private ArrayList<Procedure> procedures;
    DragListView proceduresList;
    Parcelable state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_community);

        super.onCreate(savedInstanceState);
        ActionBar supportActionBar = getSupportActionBar();
        if(supportActionBar != null)
            supportActionBar.setDisplayHomeAsUpEnabled(true);

        refreshLayout().setOnRefreshListener(this::getCommunity);


        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);

        proceduresList = findViewById(R.id.community_procedures_list);
        proceduresList.setLayoutManager(layoutManager);

        DraggableProcedureCardAdapter adapter = new DraggableProcedureCardAdapter(this, this, new ArrayList<>(), R.layout.card_procedure_draggable, R.id.card_procedure_image, true);
        proceduresList.setAdapter(adapter, true);
    }

    @Override
    protected void onResume(){
        super.onResume();

        getCommunity();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(getCurrentUser().isOperator())
            return true;

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
            case R.id.action_add_procedure:
                Intent addProcedureIntent = new Intent(CommunityActivity.this, ProcedureFormActivity.class);
                addProcedureIntent.putExtra("communityId", community.getId());
                startActivity(addProcedureIntent);
                break;
            case R.id.action_edit_community:
                Intent editCommunityIntent = new Intent(CommunityActivity.this, CommunityFormActivity.class);
                editCommunityIntent.putExtra("communityId", community.getId());
                startActivity(editCommunityIntent);
                break;
            case R.id.action_delete_community:
                AlertDialog.Builder builder = new AlertDialog.Builder(CommunityActivity.this);
                builder.setMessage(R.string.community_delete_dialog_message);
                builder.setTitle(R.string.community_delete_dialog_title);

                builder.setPositiveButton(R.string.ok, (dialog, id) ->
                        CommunitiesResource.delete(community.getId(), new Resolver() {
                            @Override
                            public void onSuccess(String data) {
                                ToastUtility.popLong(CommunityActivity.this, "Community deleted");
                                onBackPressed();
                            }

                            @Override
                            public void onError(String data) {
                                ToastUtility.popLong(CommunityActivity.this, "Could not delete community");
                            }
                        }));

                builder.setNegativeButton(R.string.cancel, (dialog, id) -> dialog.cancel());

                builder.create().show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void getCommunity() {
        if(communityId == 0) {
            communityId = getIntent().getIntExtra("communityId", 0);
        }

        CommunitiesResource.get(communityId, new Resolver() {
            @Override
            public void onSuccess(String data) {
                try {
                    community = new Community(new JSONObject(data));

                    ActionBar supportActionBar = getSupportActionBar();

                    if(supportActionBar != null)
                        supportActionBar.setDisplayShowTitleEnabled(true);

                    setTitle(community.getName());

                    boolean hasLocation = community.hasLocation();
                    boolean hasMachineNumber = !community.getMachineNumber().isEmpty();
                    boolean hasDescription = !community.getDescription().isEmpty();

                    if(hasLocation) {
                        TextView location = findViewById(R.id.community_location);
                        LinearLayout locationContainer = findViewById(R.id.community_location_container);
                        location.setText(community.getLocationName());
                        locationContainer.setVisibility(View.VISIBLE);
                    }

                    if(hasMachineNumber) {
                        TextView machineNumber = findViewById(R.id.community_machine_number);
                        LinearLayout machineNumberContainer = findViewById(R.id.community_machine_number_container);
                        machineNumber.setText(community.getMachineNumber());
                        machineNumberContainer.setVisibility(View.VISIBLE);
                    }

                    if(hasDescription) {
                        TextView description = findViewById(R.id.community_description);
                        LinearLayout descriptionContainer = findViewById(R.id.community_description_container);
                        description.setText(community.getDescription());
                        descriptionContainer.setVisibility(View.VISIBLE);
                    }

                    if(!hasLocation && !hasMachineNumber & !hasDescription) {
                        LinearLayout communityDetails = findViewById(R.id.community_details_container);
                        communityDetails.setVisibility(View.GONE);
                    }

                    fetchProceduresIfNecessary();

                    stopRefresh();
                } catch(JSONException e) {
                    Log.e(TAG, "Error parsing community JSON: " + e.getMessage());
                }
            }

            @Override
            public void onError(String data) {
                Log.e(TAG, "Error retrieving community: " + data);
                stopRefresh();
            }
        });
    }

    private void fetchProceduresIfNecessary(){
        Resolver resolver = new Resolver() {
            @Override
            public void onSuccess(String data) {
                procedures = Procedure.deserializeProcedures(data);
                initProcedureList();
            }

            @Override
            public void onError(String data) {
                Log.e(TAG, "Error retrieving procedures: " + data);
            }
        };

        if(getCurrentUser().isOperator()){
            ProceduresResource.listPublishedForCommunity(community.getId(), resolver);
        }else{
            ProceduresResource.listProcedureTemplates(community.getId(), resolver);
        }
    }

    private void initProcedureList(){
        proceduresList.setDragListListener(new DragListView.DragListListenerAdapter() {
            int scrollPosition;

            @Override
            public void onItemDragStarted(int position) {
                scrollPosition = proceduresList.getVerticalScrollbarPosition();
                refreshLayout().setEnabled(false);
            }

            @Override
            public void onItemDragging(int itemPosition, float x, float y) {
                refreshLayout().clearDisappearingChildren();

                if (y > scrollPosition) {
                    proceduresList.scrollBy(0, scrollPosition + 3);
                }

            }

            @Override
            public void onItemDragEnded(int fromPosition, int toPosition) {
                refreshLayout().setEnabled(true);
                scrollPosition = proceduresList.getVerticalScrollbarPosition();
                proceduresList.scrollTo(0, scrollPosition);

                if (fromPosition != toPosition) {
                    long id = proceduresList.getAdapter().getUniqueItemId(toPosition);
                    Log.d(TAG, "to: " + toPosition);
                    Log.d(TAG, "id: " + id);
                    ProceduresResource.update((int)id, new RequestParams("position", toPosition + 1), new Resolver() {
                        @Override
                        public void onSuccess(String data) {
                            ToastUtility.popShort(CommunityActivity.this, "Procedure order updated");
                        }

                        @Override
                        public void onError(String data) {
                            ToastUtility.popShort(CommunityActivity.this,"Error updating procedure order");
                            Log.d(TAG, "Error updating procedure order: " + data);
                        }
                    });
                }
            }
        });

        DraggableProcedureCardAdapter adapter = new DraggableProcedureCardAdapter(this, this, procedures, R.layout.card_procedure_draggable, R.id.card_procedure_handle, true);
        proceduresList.setAdapter(adapter, true);
        proceduresList.setCanDragHorizontally(false);

        if(!procedures.isEmpty()) {
            TextView noProceduresText = findViewById(R.id.community_no_procedures);
            noProceduresText.setVisibility(View.GONE);
        } else {
            proceduresList.setVisibility(View.GONE);
        }

    }

    public void navigateToProcedure(Procedure procedure){
        Intent intent;

        if(getCurrentUser().isOperator()){
            intent = new Intent(this, ExecutionActivity.class);
        }else{
            intent = new Intent(this, ProcedureActivity.class);
        }
        intent.putExtra("procedure", procedure.serialize());
        startActivity(intent);
    }
}
