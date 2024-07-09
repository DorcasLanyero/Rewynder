package com.rewyndr.rewyndr.utility;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.media.ExifInterface;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class ImageUtil {
    public static int getArea(ArrayList<Point> points){
        int crossProductSum = 0;

        int n = points.size();

        for(int i = 0; i < n; i++){
            int j = (i+1) % n;

            crossProductSum += points.get(i).x * points.get(j).y;
            crossProductSum -= points.get(j).y * points.get(j).x;
        }

        return crossProductSum/2;
    }

    public static void applyRotationIfNeeded(File imageFile){
        try {
            ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
            int rotationValue = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            int rotationAmount = 0;
            switch(rotationValue){
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotationAmount = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotationAmount = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotationAmount = 270;
                    break;
            }

            if(rotationAmount == 0)
                return;

            Bitmap bmp = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
            Matrix x = new Matrix();
            x.postRotate(rotationAmount);
            Bitmap rotatedBitmap = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), x, true);
            bmp.recycle();

            FileOutputStream stream = new FileOutputStream(imageFile);
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            rotatedBitmap.recycle();
            stream.close();
        } catch (Exception e){
            Log.e("ImageUtil", "Failed to rotate image: " + e.getMessage());
        }
    }
}
