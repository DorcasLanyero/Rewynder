package com.rewyndr.rewyndr.api.resource;

import com.loopj.android.http.RequestParams;
import com.rewyndr.rewyndr.api.resolver.Resolver;

public class CommunitiesResource extends BaseResource {
    public static void list(final Resolver resolver) {
        get("/communities", new RequestParams(), jsonArrayResponseHandler(resolver));
    }

    public static void get(int id, final Resolver resolver) {
        get( "/communities/" + Integer.toString(id), new RequestParams(), jsonObjectResponseHandler(resolver));
    }

    public static void create(RequestParams data, final Resolver resolver) {
        post("/communities", data, jsonObjectResponseHandler(resolver));
    }

    public static void update(int id, RequestParams data, final Resolver resolver) {
        patch("/communities/" + Integer.toString(id), data, jsonObjectResponseHandler(resolver));
    }

    public static void delete(int id, final Resolver resolver) {
        delete( "/communities/" + Integer.toString(id), jsonObjectResponseHandler(resolver));
    }
}
