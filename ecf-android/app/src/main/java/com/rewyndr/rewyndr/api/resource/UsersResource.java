package com.rewyndr.rewyndr.api.resource;

import com.loopj.android.http.RequestParams;
import com.rewyndr.rewyndr.api.resolver.Resolver;

public class UsersResource extends BaseResource {
    public static void update(int id, RequestParams data, final Resolver resolver) {
        patch("/users/" + Integer.toString(id), data, jsonObjectResponseHandler(resolver));
    }
}
