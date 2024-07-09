package com.rewyndr.rewyndr.utility;

import android.content.Context;
import android.graphics.PorterDuff;
import androidx.core.content.ContextCompat;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.rewyndr.rewyndr.R;

public class ToastUtility {
    public static void popLong(Context context, String message) {
        showCustomToast(context, message, Toast.LENGTH_LONG, R.color.brandBackground, R.color.white);
    }

    public static void popLongLight(Context context, String message) {
        showCustomToast(context, message, Toast.LENGTH_LONG, R.color.white, R.color.black);
    }

    public static void popShort(Context context, String message) {
        showCustomToast(context, message, Toast.LENGTH_SHORT, R.color.brandBackground, R.color.white);
    }

    private static void showCustomToast(Context context, String message, int length, int bgColor, int textColor) {
        Toast toast = Toast.makeText(context, message, length);

        View container = toast.getView();
        container.getBackground().setColorFilter(ContextCompat.getColor(context, bgColor), PorterDuff.Mode.SRC_IN);
        container.setBackgroundResource(bgColor);

        TextView text = (TextView) container.findViewById(android.R.id.message);
        text.setTextColor(ContextCompat.getColor(context, textColor));

        toast.show();
    }
}
