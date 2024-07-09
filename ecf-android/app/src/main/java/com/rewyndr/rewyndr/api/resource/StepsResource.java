package com.rewyndr.rewyndr.api.resource;

import com.loopj.android.http.RequestParams;
import com.rewyndr.rewyndr.api.resolver.Resolver;

public class StepsResource extends BaseResource {
    public static void get(int id, final Resolver resolver) {
        get( "/steps/" + Integer.toString(id), new RequestParams(), jsonObjectResponseHandler(resolver));
    }

    public static void create(RequestParams data, final Resolver resolver) {
        post("/steps", data, jsonObjectResponseHandler(resolver));
    }

    public static void saveExecutionResult(int id, RequestParams data, final Resolver resolver){
        post("/steps/" + Integer.toString(id) + "/executions", data, jsonObjectResponseHandler(resolver));
    }

    public static void update(int id, RequestParams data, final Resolver resolver) {
        patch("/steps/" + Integer.toString(id), data, jsonObjectResponseHandler(resolver));
    }

    public static void delete(int id, final Resolver resolver) {
        delete( "/steps/" + Integer.toString(id), jsonObjectResponseHandler(resolver));
    }
}
