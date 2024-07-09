package com.rewyndr.rewyndr.api.resource;

import com.loopj.android.http.RequestParams;
import com.rewyndr.rewyndr.api.resolver.Resolver;

public class LocationsResource extends BaseResource {
    public static void list(final Resolver resolver) {
        get("/locations", new RequestParams(), jsonArrayResponseHandler(resolver));
    }
}
