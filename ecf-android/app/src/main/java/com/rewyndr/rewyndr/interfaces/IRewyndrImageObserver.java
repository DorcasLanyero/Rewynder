package com.rewyndr.rewyndr.interfaces;

import android.graphics.Bitmap;
import android.graphics.Point;

import com.rewyndr.rewyndr.enums.RewyndrImageState;
import com.rewyndr.rewyndr.model.Tag;

import java.util.List;

public interface IRewyndrImageObserver {
    void imageChanged(Bitmap newImageBitmap);
    void stateUpdate(RewyndrImageState mode);
    void tagUpdate(Tag t);
    void tagBoundsUpdated(List<Point> list);
}
