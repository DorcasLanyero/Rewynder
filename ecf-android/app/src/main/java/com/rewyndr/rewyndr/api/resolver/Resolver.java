package com.rewyndr.rewyndr.api.resolver;

public abstract class Resolver {
    public abstract void onSuccess(String data);
    public abstract void onError(String data);

    public void onProgress(int progress) {};
    public void onStart() {};
    public void onFinish() {};
}
