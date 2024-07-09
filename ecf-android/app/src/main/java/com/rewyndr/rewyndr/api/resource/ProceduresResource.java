package com.rewyndr.rewyndr.api.resource;

import com.loopj.android.http.RequestParams;
import com.rewyndr.rewyndr.api.resolver.Resolver;

public class ProceduresResource extends BaseResource {
    public static void list(final Resolver resolver) {
        get("/procedures", new RequestParams(), jsonArrayResponseHandler(resolver));
    }

    public static void listPublishedForCommunity(int communityId, final Resolver resolver){
        get("/communities/"+ Integer.toString(communityId) +"/procedures?templates=true&published=true", new RequestParams(), jsonArrayResponseHandler(resolver));
    }

    public static void listProcedureTemplates(int communityId, final Resolver resolver){
        get("/communities/" + communityId + "/procedures?templates=true", new RequestParams(), jsonArrayResponseHandler(resolver));
    }

    public static void listExecutions(int procedureId, final Resolver resolver){
        get("/procedures/" + procedureId + "/executions", new RequestParams(), jsonArrayResponseHandler(resolver));
    }

    public static void get(int id, final Resolver resolver) {
        get( "/procedures/" + Integer.toString(id), new RequestParams(), jsonObjectResponseHandler(resolver));
    }

    public static void beginExecution(int procedureId, final Resolver resolver){
        post("/procedures/" + Integer.toString(procedureId) + "/executions", new RequestParams(), jsonObjectResponseHandler(resolver));
    }

    public static void create(RequestParams data, final Resolver resolver) {
        post("/procedures", data, jsonObjectResponseHandler(resolver));
    }

    public static void update(int id, RequestParams data, final Resolver resolver) {
        patch("/procedures/" + Integer.toString(id), data, jsonObjectResponseHandler(resolver));
    }

    public static void delete(int id, final Resolver resolver) {
        delete( "/procedures/" + Integer.toString(id), jsonObjectResponseHandler(resolver));
    }
}
