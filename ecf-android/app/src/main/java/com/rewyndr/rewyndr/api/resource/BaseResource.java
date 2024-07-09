package com.rewyndr.rewyndr.api.resource;

import com.loopj.android.http.*;
import com.rewyndr.rewyndr.BuildConfig;
import com.rewyndr.rewyndr.api.resolver.ImageResolver;
import com.rewyndr.rewyndr.api.resolver.Resolver;
import com.rewyndr.rewyndr.utility.FileUtility;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.ContentType;
import cz.msebera.android.httpclient.entity.StringEntity;

public abstract class BaseResource {
    private static final String TAG = "BaseResource";
    private static final String BASE_API_URL = BuildConfig.DEBUG ? BuildConfig.API_BASE_URL_ST : BuildConfig.API_BASE_URL_PR;
    private static final String AUTHORIZATION_HEADER = "Authorization";

    private static AsyncHttpClient client = new AsyncHttpClient();
    private static TokenStore tokenStore;
    private static boolean initialized;

    public static void init(Context context) {
        if(!initialized) {
            tokenStore = new TokenStore(context);

            if(tokenStore.hasAuthenticationToken()) {
                client.addHeader(AUTHORIZATION_HEADER, "Bearer " + tokenStore.getAuthenticationToken());
            }

            // Set long socket/connection timeout for slow image uploads
            client.setTimeout(50000);

            initialized = true;
        }
    }

    public static boolean isAuthenticated() {
        return tokenStore.hasAuthenticationToken();
    }

    public static void setAuthenticationToken(String token) {
        tokenStore.setAuthenticationToken(token);
        client.addHeader(AUTHORIZATION_HEADER, "Bearer " + token);
    }

    public static void unsetAuthenticationToken() {
        tokenStore.deleteAuthenticationToken();
        client.removeHeader(AUTHORIZATION_HEADER);
    }

    public static void getImage(Context context, String url, final ImageResolver resolver) {
        if(url == null) { return; }

        client.get(url, new FileAsyncHttpResponseHandler(context) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, File file) {
                resolver.onSuccess(Drawable.createFromPath(file.getAbsolutePath()));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
                resolver.onError(Drawable.createFromPath(file.getAbsolutePath()));
            }
        });
    }

    protected static void get(String path, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        if(params == null) params = new RequestParams();
        logRequest("GET", path, params);
        client.get(getApiUrl(path), params, responseHandler);
    }

    protected static void post(String path, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        logRequest("POST", path, params);
        client.post(getApiUrl(path), params, responseHandler);
    }

    protected static void post(Context context, String path, JSONObject data, AsyncHttpResponseHandler responseHandler) {
        logRequest("POST", path, data);
        client.post(context, getApiUrl(path), createJsonPayload(data), "application/json", responseHandler);
    }

    protected static void patch(String path, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        logRequest("PATCH", path, params);
        client.patch(getApiUrl(path), params, responseHandler);
    }

    protected static void patch(Context context, String path, JSONObject data, AsyncHttpResponseHandler responseHandler) {
        logRequest("PATCH", path, data);
        client.patch(context, getApiUrl(path), createJsonPayload(data), "application/json", responseHandler);
    }

    protected static void delete(String path, AsyncHttpResponseHandler responseHandler) {
        logRequest("DELETE", path);
        client.delete(getApiUrl(path), responseHandler);
    }

    protected static void upload(String path, String fileKey, String filePath, AsyncHttpResponseHandler responseHandler) {
        RequestParams params = new RequestParams();

        try {
            params.put(fileKey, new File(filePath), FileUtility.getMimeType(filePath));
            post(path, params, responseHandler);
        } catch(FileNotFoundException e) {
            Log.e(TAG, "File not found: " + filePath);
        }
    }

    protected static StringEntity createJsonPayload(JSONObject data) {
        return new StringEntity(data.toString(), ContentType.APPLICATION_JSON);
    }

    private static class GenericJsonHttpResponseHandler extends JsonHttpResponseHandler {
        private Resolver _resolver;

        public GenericJsonHttpResponseHandler(Resolver resolver) {
            _resolver = resolver;
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject errorResponse) {
            if(_resolver != null && errorResponse != null) _resolver.onError(errorResponse.toString());
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, String message, Throwable e) {
            if(_resolver != null) _resolver.onError(message);
        }

        @Override
        public void onProgress(long position, long length) {
            int progress = (int)(((float)position / (float)length) * 100);
            if(_resolver != null) _resolver.onProgress(progress);
        }

        @Override
        public void onStart() {
            if(_resolver != null) _resolver.onStart();
        }

        @Override
        public void onFinish() { if(_resolver != null) _resolver.onFinish(); }

    }

    protected static JsonHttpResponseHandler jsonObjectResponseHandler(final Resolver resolver) {
        return new GenericJsonHttpResponseHandler(resolver) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if (resolver != null) resolver.onSuccess(response.toString());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject errorResponse) {
                if(resolver != null && errorResponse != null) resolver.onError(errorResponse.toString());
            }
        };
    }

    protected static JsonHttpResponseHandler jsonArrayResponseHandler(final Resolver resolver) {
        return new GenericJsonHttpResponseHandler(resolver) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                if (resolver != null) resolver.onSuccess(response.toString());
            }
        };
    }

    private static String getApiUrl(String path) {
        return BASE_API_URL + path;
    }

    private static void logRequest(String httpVerb, String path) {
        Log.d("HTTP CLIENT REQUEST", httpVerb + ": " + path);
    }

    private static void logRequest(String httpVerb, String path, JSONObject data) {
        Log.d("HTTP CLIENT REQUEST", httpVerb + ": " + path + "; DATA: " + data.toString());
    }

    private static void logRequest(String httpVerb, String path, RequestParams params) {
        Log.d("HTTP CLIENT REQUEST", httpVerb + ": " + path + "; PARAMS: " + params.toString());
    }

    private static void logRequest(String httpVerb, String path, StringEntity params) {
        Log.d("HTTP CLIENT REQUEST", httpVerb + ": " + path + "; PARAMS: " + params.toString());
    }
}
