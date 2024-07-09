package com.rewyndr.rewyndr.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.bumptech.glide.Glide;
import com.rewyndr.rewyndr.R;
import com.rewyndr.rewyndr.api.resolver.Resolver;
import com.rewyndr.rewyndr.api.resource.CommunitiesResource;
import com.rewyndr.rewyndr.model.Community;
import com.rewyndr.rewyndr.utility.ToastUtility;
import com.rewyndr.rewyndr.view.SquareImageView;

import java.util.ArrayList;

public class CommunitiesActivity extends BaseActivity {
    private static String TAG = "CommunitiesActivity";
    private ArrayList<Community> communities;
    private ArrayList<Community> communitiesToDisplay = new ArrayList<>();
    private Menu menu;

    private GridView parentView;
    private CommunityCardAdapter adapter;

    private EditText searchText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        hasSideMenu = true;
        setContentView(R.layout.activity_communities);

        super.onCreate(savedInstanceState);

        searchText = findViewById(R.id.search_text);

        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterCommunities(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}

            @Override
            public void afterTextChanged(Editable s) {}
        });

        getSupportActionBar().setDisplayShowTitleEnabled(true);
        setTitle("Communities");

        refreshLayout().setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getCommunities();
            }
        });
    }

    @Override
    protected void onResume(){
        super.onResume();

        getCommunities();
    }

    private void filterCommunities(String s){
        if(communities == null || communities.isEmpty())
            return;

        communitiesToDisplay.clear();

        if(s.length() == 0){
            communitiesToDisplay.addAll(communities);
        }else {
            for (Community community : communities) {
                String compare = s.toLowerCase();
                if (stringContains(community.getName(), compare) || stringContains(community.getMachineNumber(), compare) ||
                        stringContains(community.getLocationName(), compare)) {
                    communitiesToDisplay.add(community);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }

    boolean stringContains(String haystack, String needle){
        return haystack != null && haystack.toLowerCase().contains(needle);
    }

    // Exit the application when back is pressed
    @Override
    public void onBackPressed() {
        exitApplication();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(getCurrentUser().isOperator())
            return true;

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.communities_menu, menu);
        this.menu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_community:
                Intent intent = new Intent(CommunitiesActivity.this, CommunityFormActivity.class);
                startActivity(intent);
                break;
        }


        return super.onOptionsItemSelected(item);
    }

    public void getCommunities() {
        CommunitiesResource.list(new Resolver() {
            @Override
            public void onSuccess(String data) {
                communities = Community.deserializeCommunities(data);
                communitiesToDisplay.clear();
                communitiesToDisplay.addAll(communities);
                parentView = (GridView) findViewById(R.id.parentView);
                adapter = new CommunityCardAdapter(CommunitiesActivity.this);
                parentView.setAdapter(adapter);

                filterCommunities(searchText.getText().toString());
                stopRefresh();
            }

            @Override
            public void onError(String data) {
                ToastUtility.popLong(CommunitiesActivity.this, "Could not retrieve communities.");
                stopRefresh();
            }
        });
    }

    private class CommunityCardAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private Context mContext;

        public CommunityCardAdapter(Context context) {
            this.mContext = context;
            this.mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return communitiesToDisplay.size();
        }

        @Override
        public Object getItem(int item) {
            return item;
        }

        @Override
        public long getItemId(int id) {
            return id;
        }

        private class CommunityCardViewHolder {
            public SquareImageView image;
            public TextView name;
            public TextView location;
            public int position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parentView) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.card_community, parentView, false);
                final CommunityCardViewHolder holder = new CommunityCardViewHolder();

                holder.image = (SquareImageView) convertView.findViewById(R.id.card_community_image);
                holder.name = (TextView) convertView.findViewById(R.id.card_community_name);
                holder.location = (TextView) convertView.findViewById(R.id.card_community_location);

                convertView.setTag(holder);
            }

            final CommunityCardViewHolder holder = (CommunityCardViewHolder) convertView.getTag();

            final Community community = communitiesToDisplay.get(position);

            holder.position = position;
            holder.name.setText(community.getName());
            holder.location.setText(community.getLocationName());
            holder.image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(getCurrentUser().isOperator()){
                        navigateToActivity(CommunityActivity.class, community);
                    }else {
                        promptUserCommunitySelection(community);
                    }
                }
            });

            TextDrawable td = TextDrawable.builder().buildRect(community.getName().substring(0,1), R.color.brandLightBackground);
            Glide.with(CommunitiesActivity.this).load(community.getImageUrl())
                    .placeholder(td).into(holder.image);

            return convertView;
        }
    }

    private void promptUserCommunitySelection(Community community){
        AlertDialog.Builder builder = new AlertDialog.Builder(CommunitiesActivity.this);

        builder.setMessage(R.string.procedure_selection_prompt);

        builder.setPositiveButton(R.string.edit_procedures, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface d, int i){
                navigateToActivity(CommunityActivity.class, community);
            }
        });

        builder.setNegativeButton(R.string.view_procedure_history, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface d, int i){
                navigateToActivity(ProcedureHistoryListActivity.class, community);
            }
        });

        builder.show();
    }

    private void navigateToActivity(Class activityClass, Community community){
        Intent intent = new Intent(CommunitiesActivity.this, activityClass);
        intent.putExtra("communityId", community.getId());
        intent.putExtra("communityName", community.getName());
        CommunitiesActivity.this.startActivity(intent);
    }
}
