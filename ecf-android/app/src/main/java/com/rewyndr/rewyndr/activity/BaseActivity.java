package com.rewyndr.rewyndr.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.navigation.NavigationView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.rewyndr.rewyndr.R;
import com.rewyndr.rewyndr.api.resolver.Resolver;
import com.rewyndr.rewyndr.api.resource.BaseResource;
import com.rewyndr.rewyndr.api.resource.SessionsResource;
import com.rewyndr.rewyndr.model.User;
import com.rewyndr.rewyndr.utility.ToastUtility;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class BaseActivity extends AppCompatActivity {
    private static String TAG = "BaseActivity";
    protected boolean hasSideMenu = false;
    private ActionBarDrawerToggle sideMenuToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BaseResource.init(this);

        if(hasSideMenu) {
            NavigationView sideMenu = (NavigationView) findViewById(R.id.side_menu);
            sideMenu.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch(item.getItemId()) {
                        case R.id.menu_item_communities:
                            navigateToCommunities();
                            break;
                        case R.id.menu_item_account:
                            navigateToAccount();
                            break;
                        case R.id.menu_item_logout:
                            signOut();
                            break;
                    }

                    return true;
                }
            });

			DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

			// Remove shadow from drawer
			drawerLayout.setScrimColor(getResources().getColor(android.R.color.transparent));

			sideMenuToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.app_name, R.string.app_name);
			drawerLayout.addDrawerListener(sideMenuToggle);

			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (hasSideMenu){
            sideMenuToggle.syncState();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (hasSideMenu){
            sideMenuToggle.onConfigurationChanged(newConfig);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (hasSideMenu){
            if (sideMenuToggle.onOptionsItemSelected(item)) {
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    protected String errorMessage(String root, String data) {
        try {
            JSONObject response = new JSONObject(data);
            String error = response.getString("error");

            if(error.isEmpty()) {
                return root;
            }

            return root + ": " + error;
        } catch (JSONException e) {
            return root;
        }
    }

    protected void exitApplication() {
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }

    // --- CURRENT USER
    private String CURRENT_USER_STORE = "rewyndr-current-user-store";
    private String CURRENT_USER_KEY = "currentUser";

    private SharedPreferences getCurrentUserStore() {
        return getSharedPreferences(CURRENT_USER_STORE, Context.MODE_PRIVATE);
    }

    protected User getCurrentUser() {
        String userJSON = getCurrentUserStore().getString(CURRENT_USER_KEY, null);
        try {
            return new User(new JSONObject(userJSON));
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing current user JSON: " + e.getMessage());
        }

        return null;
    }

    protected void setCurrentUser(String userJSON) {
        SharedPreferences.Editor editor = getCurrentUserStore().edit();
        editor.putString(CURRENT_USER_KEY, userJSON);
        editor.commit();
    }

    protected void clearCurrentUser() {
        SharedPreferences.Editor editor = getCurrentUserStore().edit();
        editor.clear();
        editor.commit();
    }
    // ---

    private void navigateToCommunities() {
        Intent intent = new Intent(BaseActivity.this, CommunitiesActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void navigateToAccount() {
        Intent intent = new Intent(BaseActivity.this, UserFormActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void signOut() {
        SessionsResource.delete(new Resolver() {
            @Override
            public void onSuccess(String data) {
                BaseResource.unsetAuthenticationToken();
                clearCurrentUser();

                ToastUtility.popLongLight(BaseActivity.this, "You have signed out successfully.");

                Intent intent = new Intent(BaseActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("signedOut", true);
                startActivity(intent);
            }

            @Override
            public void onError(String data) {
                ToastUtility.popShort(BaseActivity.this, "Error signing out.");
            }
        });
    }


    // Utility methods
    protected SwipeRefreshLayout refreshLayout() {
        return (SwipeRefreshLayout) findViewById(R.id.pull_to_refresh);
    }

    protected void stopRefresh() {
        SwipeRefreshLayout pullToRefresh = refreshLayout();
        if(pullToRefresh != null) {
            pullToRefresh.setRefreshing(false);
        }
    }

    protected void hideKeyboard(){
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View v = getWindow().getDecorView().getRootView();
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }
}
