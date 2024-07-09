package com.rewyndr.rewyndr.api.resource;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenStore {
    private final String STORE_NAME = "rewyndr-token-store";
    private final String AUTHENTICATION_TOKEN_KEY = "authentication_token";
    private SharedPreferences store;

    public TokenStore(Context context) {
        store = context.getSharedPreferences(STORE_NAME, Context.MODE_PRIVATE);
    }

    public void deleteAuthenticationToken() {
        SharedPreferences.Editor storeEditor = store.edit();
        storeEditor.clear();
        storeEditor.commit();
    }

    public String getAuthenticationToken() {
        return store.getString(AUTHENTICATION_TOKEN_KEY, null);
    }

    public boolean hasAuthenticationToken() {
        return getAuthenticationToken() != null;
    }

    public void setAuthenticationToken(String token) {
        SharedPreferences.Editor storeEditor = store.edit();
        storeEditor.putString(AUTHENTICATION_TOKEN_KEY, token);
        storeEditor.commit();
    }
}