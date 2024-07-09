package com.rewyndr.rewyndr.api.resource;

import com.loopj.android.http.RequestParams;
import com.rewyndr.rewyndr.api.resolver.Resolver;

public class AnnotationsResource extends BaseResource {
    public static void list(final Resolver resolver) {
        get("/annotations", new RequestParams(), jsonArrayResponseHandler(resolver));
    }

    public static void get(int id, final Resolver resolver) {
        get( "/annotations/" + Integer.toString(id), new RequestParams(), jsonObjectResponseHandler(resolver));
    }

    public static void create(RequestParams data, final Resolver resolver) {
        post("/annotations", data, jsonObjectResponseHandler(resolver));
    }

    public static void update(int id, RequestParams data, final Resolver resolver) {
        patch("/annotations/" + Integer.toString(id), data, jsonObjectResponseHandler(resolver));
    }

    public static void delete(int id, final Resolver resolver) {
        delete( "/annotations/" + Integer.toString(id), jsonObjectResponseHandler(resolver));
    }
}
