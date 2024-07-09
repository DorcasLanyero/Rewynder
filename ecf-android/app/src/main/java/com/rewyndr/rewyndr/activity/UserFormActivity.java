package com.rewyndr.rewyndr.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;
import com.rewyndr.rewyndr.R;
import com.rewyndr.rewyndr.api.resolver.Resolver;
import com.rewyndr.rewyndr.api.resource.UsersResource;
import com.rewyndr.rewyndr.utility.ToastUtility;
import com.rewyndr.rewyndr.utility.VersionUtility;

public class UserFormActivity extends BaseActivity {
    private static String TAG = "UserFormActivity";

    private TextView versionNameTextView;

    // Inputs
    private EditText passwordInput;
    private EditText passwordConfirmationInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.hasSideMenu = true;

        setContentView(R.layout.activity_user_form);
        super.onCreate(savedInstanceState);

        versionNameTextView = findViewById(R.id.version_name);
        versionNameTextView.setText(VersionUtility.getVersionName(UserFormActivity.this));

        passwordInput = (EditText) findViewById(R.id.user_password_input);
        passwordConfirmationInput = (EditText) findViewById(R.id.user_password_confirmation_input);

        getSupportActionBar().setDisplayShowTitleEnabled(true);
        setTitle("Manage Account ~ " + getCurrentUser().getUsername());

        Button saveButton = (Button)findViewById(R.id.save_user_button);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RequestParams data = new RequestParams();

                String password = passwordInput.getText().toString();
                String passwordConfirmation = passwordConfirmationInput.getText().toString();

                if(!password.equals(passwordConfirmation)) {
                    ToastUtility.popLong(UserFormActivity.this, "Password and confirmation must match");
                    return;
                }

                data.put("password", password);
                data.put("password_confirmation", passwordConfirmation);

                Resolver saveUserResolver = new Resolver() {
                    @Override
                    public void onSuccess(String data) {
                        ToastUtility.popLong(UserFormActivity.this, "Account updated");
                        onBackPressed();
                    }

                    @Override
                    public void onError(String data) {
                        Log.e(TAG, "Error updating account");
                        ToastUtility.popLong(UserFormActivity.this, errorMessage("Error updating account", data));
                    }
                };

                UsersResource.update(getCurrentUser().getId(), data, saveUserResolver);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}

