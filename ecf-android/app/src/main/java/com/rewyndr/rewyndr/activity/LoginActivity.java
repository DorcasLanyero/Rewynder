package com.rewyndr.rewyndr.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;

import android.os.Build;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.rewyndr.rewyndr.R;
import com.rewyndr.rewyndr.api.resolver.Resolver;
import com.rewyndr.rewyndr.api.resource.BaseResource;
import com.rewyndr.rewyndr.api.resource.SessionsResource;
import com.rewyndr.rewyndr.model.User;
import com.rewyndr.rewyndr.utility.ToastUtility;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A login screen that offers login via username/password.
 */
public class LoginActivity extends BaseActivity  {
    private String TAG = "LoginActivity";

    // UI references
    private EditText mUsernameView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Set up login form
        mUsernameView = (EditText) findViewById(R.id.username);

        mUsernameView.addTextChangedListener(new TextWatcher(){
            @Override
            public void afterTextChanged(Editable s){
                String result = s.toString().trim();

                if(mUsernameView.getText().toString().compareTo(result) != 0) {
                    mUsernameView.setText(result);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence sequence, int i1, int i2, int i3){}

            @Override
            public void onTextChanged(CharSequence sequence, int i1, int i2, int i3){}
        });

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button signInButton = (Button) findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = getIntent();

        /**
         * If the user has an authentication token stored on the device and they are not
         * signing out, attempt to retrieve their data and move beyond the login activity
         */
        if(BaseResource.isAuthenticated() && !intent.getBooleanExtra("signedOut", false)) {
            showProgress(true);

            SessionsResource.current(new Resolver() {
                @Override
                public void onSuccess(String data) {
                    setCurrentUserAndEnter(data, false);
                }

                @Override
                public void onError(String data) {
                    BaseResource.unsetAuthenticationToken();
                    showProgress(false);
                }
            });
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        showProgress(false);
    }

    @Override
    public void onBackPressed() {
        exitApplication();
    }

    /**
     * Attempt to sign in the user
     */
    private void attemptLogin() {
        // Reset errors
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt
        String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Ensure that username is provided
        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        }

        // Ensure that password is provided
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            SessionsResource.create(username, password, new Resolver() {
                @Override
                public void onSuccess(String data) {
                    setCurrentUserAndEnter(data, true);

                    ActivityCompat.requestPermissions(LoginActivity.this,
                            new String[]{
                                    Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.CAMERA,
                                    Manifest.permission.RECORD_AUDIO},
                            1);
                }

                @Override
                public void onError(String data) {
                    ToastUtility.popLongLight(LoginActivity.this, "Invalid username or password.");
                    showProgress(false);
                }
            });
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void setCurrentUserAndEnter(String userData, boolean setToken) {
        try {
            User user = new User(new JSONObject(userData));

            if(setToken) {
                BaseResource.setAuthenticationToken(user.getToken());
            }

            setCurrentUser(userData);

            Intent intent = new Intent(LoginActivity.this, CommunitiesActivity.class);
            startActivity(intent);
        } catch (JSONException e) {
            ToastUtility.popShort(LoginActivity.this, "Oops. Something went awry.");
            showProgress(false);
            Log.e(TAG, "Error deserializing user JSON: " + e.getMessage());
        }
    }
}
