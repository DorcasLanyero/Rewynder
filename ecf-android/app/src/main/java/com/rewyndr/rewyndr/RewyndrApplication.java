package com.rewyndr.rewyndr;

import android.app.Application;
import android.content.Context;

public class RewyndrApplication extends Application {
    private static Application instance;

    public static Application getInstance(){
        return instance;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        instance = this;
    }
}
