package com.rewyndr.rewyndr.api.resource;

import android.content.Context;
import com.loopj.android.http.RequestParams;
import com.rewyndr.rewyndr.api.resolver.Resolver;

import java.util.HashMap;

public class SessionsResource extends BaseResource {
    public static void create(String username, String password, final Resolver resolver) {
        HashMap<String, String> data = new HashMap<>();
        data.put("username", username);
        data.put("password", password);

        post("/sessions", new RequestParams(data), jsonObjectResponseHandler(resolver));
    }

    public static void delete(final Resolver resolver) {
        delete("/sessions", jsonObjectResponseHandler(resolver));
    }

    public static void current(final Resolver resolver) {
        get("/sessions/current", new RequestParams(), jsonObjectResponseHandler(resolver));
    }
}
