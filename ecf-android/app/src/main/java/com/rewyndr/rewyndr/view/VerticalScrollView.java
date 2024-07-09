package com.rewyndr.rewyndr.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class VerticalScrollView extends ScrollView{
    private boolean enabled;

    public VerticalScrollView(Context context) {
        super(context);
        this.enabled = true;
    }

    public VerticalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.enabled = true;
    }

    public VerticalScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.enabled = true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(this.enabled) {
            final int action = ev.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    Log.i("VerticalScrollview", "onInterceptTouchEvent: DOWN super false");
                    super.onTouchEvent(ev);
                    break;

                case MotionEvent.ACTION_MOVE:
                    return false; // redirect MotionEvents to ourself

                case MotionEvent.ACTION_CANCEL:
                    Log.i("VerticalScrollview", "onInterceptTouchEvent: CANCEL super false");
                    super.onTouchEvent(ev);
                    break;

                case MotionEvent.ACTION_UP:
                    Log.i("VerticalScrollview", "onInterceptTouchEvent: UP super false");
                    return false;

                default:
                    Log.i("VerticalScrollview", "onInterceptTouchEvent: " + action);
                    break;
            }
        }

        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if(this.enabled) {
            return super.onTouchEvent(ev);
        }
        return false;
    }

    public void setScrollingEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}