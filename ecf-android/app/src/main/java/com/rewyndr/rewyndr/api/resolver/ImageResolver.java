package com.rewyndr.rewyndr.api.resolver;

import android.graphics.drawable.Drawable;

public abstract class ImageResolver {
    public abstract void onSuccess(Drawable image);
    public abstract void onError(Drawable image);
}
