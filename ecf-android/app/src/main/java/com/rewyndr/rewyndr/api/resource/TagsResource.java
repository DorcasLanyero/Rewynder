package com.rewyndr.rewyndr.api.resource;

import android.content.Context;

import com.loopj.android.http.RequestParams;
import com.rewyndr.rewyndr.api.resolver.Resolver;

import org.json.JSONObject;

public class TagsResource extends BaseResource {
    public static void get(int id, final Resolver resolver) {
        get( "/tags/" + Integer.toString(id), new RequestParams(), jsonObjectResponseHandler(resolver));
    }

    public static void create(Context context, JSONObject data, final Resolver resolver) {
        post(context,"/tags", data, jsonObjectResponseHandler(resolver));
    }

    public static void update(Context context, int id, JSONObject data, final Resolver resolver) {
        patch(context,"/tags/" + Integer.toString(id), data, jsonObjectResponseHandler(resolver));
    }

    public static void delete(int id, final Resolver resolver) {
        delete( "/tags/" + Integer.toString(id), jsonObjectResponseHandler(resolver));
    }
}