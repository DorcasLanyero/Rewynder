package com.rewyndr.rewyndr.boundary;

import java.util.Comparator;
import java.util.List;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;

import com.rewyndr.rewyndr.model.Tag;

public class ImageProcessor {

    private static class ImageBounds {
        public int left;
        public int right;
        public int top;
        public int bottom;
    }

    public final static int IMAGE_BORDER_WIDTH = 20;
    public final static int ICON_DIM = 70;
    public final static int ICON_OFFSET = 20;

    /**
     * Draws tags on the bitmap, where originalBitmap is the one
     * we get from the rewyndr(LargeImage).
     */
    public static Bitmap generateTaggedBitmap(Context context, Bitmap originalBitmap, List<Tag> tagList) {
        Bitmap bitmap = originalBitmap;

        // Create buffer new bitmap
        Bitmap newBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawBitmap(bitmap, 0, 0, null);

        tagList.sort((t1, t2) -> t2.getCreatedDate().compareTo(t1.getCreatedDate()));

        // Draw tags based on tag coordinate
        for (Tag tag : tagList) {
            List<Point> points = tag.getPoints();

            Point prevPoint = points.get(0);

            drawIcon(context, canvas, prevPoint, tag);

            for (int i = 1; i <= points.size() - 1; i++) {
                Point currentPoint = points.get(i);
                canvas.drawLine(prevPoint.x, prevPoint.y, currentPoint.x, currentPoint.y, getTagBrush(Color.parseColor(tag.getBoundaryColor())));
                prevPoint = currentPoint;
            }

            canvas.drawLine(prevPoint.x, prevPoint.y, points.get(0).x, points.get(0).y, getTagBrush(Color.parseColor(tag.getBoundaryColor())));
        }

        return newBitmap;
    }

    /**
     * Handles highlighting of tag:
     *
     * - If the user touches a tag, then it will darken the rest of the image
     * - If the user does not touch a tag, then it will just show the image with all tags
     */
    public static Bitmap generateHighlightedTaggedBitmap(Context context,
                                                         Bitmap originalBitmap,
                                                         Bitmap taggedBitmap,
                                                         Bitmap darkenTaggedBitmap,
                                                         List<Tag> tagList,
                                                         float x,
                                                         float y) {
        // Dark bitmap as the background
        Bitmap bitmap = darkenTaggedBitmap;

        // Create buffer new bitmap
        Bitmap newBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR);

        // Loop through the tag list and check whether the (x, y) is in any tag
        Tag tag = getSelectedTag(tagList, x, y);

        if (tag != null) {
            List<Point> points = tag.getPoints();

            // Generate brush
            Paint paint = new Paint();
            paint.setColor(Color.WHITE);

            // Set paint to draw polygon
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.FILL);
            Path path = new Path();

            // Create mask polygon
            path.moveTo(points.get(0).x, points.get(0).y);
            for (int i = 1; i <= points.size() - 1; i++) {
                path.lineTo(points.get(i).x, points.get(i).y);
            }
            path.close();
            canvas.drawPath(path, paint);

            Point prevPoint = points.get(0);

            // Create lighten selected area
            paint.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(originalBitmap, 0, 0, paint);

            drawIcon(context, canvas, prevPoint, tag);

            // Create darken background
            paint.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.DST_OVER));
            canvas.drawBitmap(darkenTaggedBitmap, 0, 0, paint);

            // Draw polygon lines
            for (int i = 1; i <= points.size() - 1; i++) {
                Point currentPoint = points.get(i);
                canvas.drawLine(prevPoint.x, prevPoint.y, currentPoint.x, currentPoint.y, getTagBrush(Color.parseColor(tag.getBoundaryColor())));
                prevPoint = currentPoint;
            }

            canvas.drawLine(prevPoint.x, prevPoint.y, points.get(0).x, points.get(0).y, getTagBrush(Color.parseColor(tag.getBoundaryColor())));
        }

        return tag != null ? newBitmap : taggedBitmap;
    }

    public static Tag getSelectedTag(List<Tag> tagList, float x, float y) {
        tagList.sort((t1, t2) -> t1.getCreatedDate().compareTo(t2.getCreatedDate()));
        for (Tag tag : tagList) {
            if(tag.contains(x, y)) {
                return tag;
            }
        }

        return null;
    }

    // This function is used to draw the edit square on the image.
    public static Bitmap drawEditSquare(Bitmap originalBitmap,
                                        Bitmap darkenOriginalBitmap, int upLeftX, int upLeftY,
                                        int bottomRightX, int bottomRightY, int color, boolean withLittleSquare) {
        Bitmap bitmap = darkenOriginalBitmap;
        // Create buffer new bitmap
        Bitmap newBitmap = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawBitmap(bitmap, 0, 0, null);

        // Guarantee the coordinate is in the boundary
        // The parameter 20 can be changed
        // This check guarantee the little square is always visible to users
        upLeftX = Math.max(IMAGE_BORDER_WIDTH, upLeftX);
        upLeftY = Math.max(IMAGE_BORDER_WIDTH, upLeftY);
        bottomRightX = Math.min(bottomRightX, originalBitmap.getWidth()
                - IMAGE_BORDER_WIDTH);
        bottomRightY = Math.min(bottomRightY, originalBitmap.getHeight()
                - IMAGE_BORDER_WIDTH);

        // Copy the lighter sub image
        copySubImage(originalBitmap, newBitmap, upLeftX, upLeftY, bottomRightX
                - upLeftX, bottomRightY - upLeftY);

        // Generate brush for draw big square
        Paint paint = getTagBrush(color);

        // Draw the square
        RectF rect = new RectF(upLeftX, upLeftY, bottomRightX, bottomRightY);
        canvas.drawRoundRect(rect, 2, 2, paint);

        if (withLittleSquare) {
            // Draw the little square on the board
            int offset = 1;
            int[] littleSquareXList = { upLeftX - offset,
                    (bottomRightX + upLeftX) / 2, bottomRightX + offset,
                    upLeftX - offset, bottomRightX + offset, upLeftX - offset,
                    (bottomRightX + upLeftX) / 2, bottomRightX + offset };
            int[] littleSquareYList = { upLeftY - offset, upLeftY - offset,
                    upLeftY - offset, (bottomRightY + upLeftY) / 2,
                    (bottomRightY + upLeftY) / 2, bottomRightY + offset,
                    bottomRightY + offset, bottomRightY + offset };
            int littleSquareRadius = 6;
            for (int i = 0; i <= littleSquareXList.length - 1; i++) {
                // Generate brush for draw little square border
                paint = new Paint();
                paint.setColor(Color.BLACK);
                paint.setStyle(Paint.Style.FILL);
                // Draw the little square border
                rect = new RectF(littleSquareXList[i] - littleSquareRadius - 2,
                        littleSquareYList[i] - littleSquareRadius - 2,
                        littleSquareXList[i] + littleSquareRadius + 2,
                        littleSquareYList[i] + littleSquareRadius + 2);
                canvas.drawRoundRect(rect, 2, 2, paint);

                // Generate brush for draw little square
                paint = new Paint();
                paint.setColor(Color.WHITE);
                paint.setStyle(Paint.Style.FILL);
                // Draw the little square
                rect = new RectF(littleSquareXList[i] - littleSquareRadius,
                        littleSquareYList[i] - littleSquareRadius,
                        littleSquareXList[i] + littleSquareRadius,
                        littleSquareYList[i] + littleSquareRadius);
                canvas.drawRoundRect(rect, 2, 2, paint);
            }
        }

        canvas.drawPoint(upLeftX, upLeftY, paint);
        return newBitmap;
    }

    /**
     * Draws the edit square on the image.
     */
    public static Bitmap drawEditSquare(Bitmap originalBitmap, Bitmap darkenOriginalBitmap, RectF rect, boolean withLittleSquare) {
        return drawEditSquare(originalBitmap,
                darkenOriginalBitmap,
                (int) rect.left,
                (int) rect.top,
                (int) rect.right,
                (int) rect.bottom,
                Color.WHITE,
                withLittleSquare);
    }

    /**
     * Darkens an image by adding a fixed value to each channel.
     */
    public static Bitmap generateDarkenImage(Bitmap originalBitmap, int value) {
        Bitmap bitmap = originalBitmap;

        Xfermode xDarken = new PorterDuffXfermode(PorterDuff.Mode.DARKEN);
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        Bitmap newBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setARGB(value, 0, 0, 0);
        p.setXfermode(xDarken);

        Canvas canvas = new Canvas(newBitmap);
        canvas.drawRect(0, 0, w, h, p);

        return newBitmap;
    }

    /**
     * Copies a sub-selection of one bitmap to another bitmap.
     */
    private static void copySubImage(Bitmap sourceImage, Bitmap targetImage, int upLeftX, int upLeftY, int width, int height) {
        // Guarantee the coordinate is in the boundary
        int startX = Math.max(0, upLeftX);
        int endX = Math.min(sourceImage.getWidth() - 1, upLeftX + width);
        int startY = Math.max(0, upLeftY);
        int endY = Math.min(sourceImage.getHeight() - 1, upLeftY + height);

        Rect src = new Rect(startX, startY, endX, endY);

        Canvas canvas = new Canvas(targetImage);
        canvas.drawBitmap(sourceImage, src, src, null);
        return;
    }

    private static Paint getTagBrush(int strokeColor) {
        Paint tagBrush = new Paint();
        tagBrush.setColor(strokeColor);
        tagBrush.setStyle(Paint.Style.STROKE);
        tagBrush.setStrokeWidth(3);
        return tagBrush;
    }

    private static void drawIcon(Context context, Canvas canvas, Point origin, Tag tag) {
        if(tag.getIcon() == null || tag.getIcon().equals("null") || tag.getIcon().isEmpty()) {
            return;
        }

        ImageBounds bounds = getIconBoundsFromOrigin(origin);

        Drawable d = ContextCompat.getDrawable(context, context.getResources().getIdentifier("safety_icon_" + tag.getIcon(), "drawable", context.getPackageName()));
        d.setBounds(bounds.left, bounds.top, bounds.right, bounds.bottom);
        d.draw(canvas);

        Paint iconPaint = new Paint();
        iconPaint.setColor(Color.RED);
        iconPaint.setStyle(Paint.Style.STROKE);
        iconPaint.setStrokeWidth(3);

        Path iconPath = new Path();
        iconPath.moveTo(bounds.left, bounds.top);
        iconPath.lineTo(bounds.right, bounds.top);
        iconPath.lineTo(bounds.right, bounds.bottom);
        iconPath.lineTo(bounds.left, bounds.bottom);
        iconPath.close();
        canvas.drawPath(iconPath, iconPaint);
    }

    private static ImageBounds getIconBoundsFromOrigin(Point origin){
        ImageBounds iconBounds = new ImageBounds();

        iconBounds.left = origin.x - (ICON_DIM + ICON_OFFSET);
        iconBounds.top = origin.y - (ICON_DIM + ICON_OFFSET);
        iconBounds.right = origin.x - ICON_OFFSET;
        iconBounds.bottom = origin.y - ICON_OFFSET;

        if(shouldShiftIconDown(origin) && shouldShiftIconRight(origin)){
            iconBounds.left = origin.x + ICON_OFFSET;
            iconBounds.top = origin.y + ICON_OFFSET;
            iconBounds.right = origin.x + (ICON_DIM + ICON_OFFSET);
            iconBounds.bottom = origin.y + (ICON_DIM + ICON_OFFSET);
        }else if (shouldShiftIconRight(origin)){
            iconBounds.left = origin.x;
            iconBounds.right = origin.x + ICON_DIM;
        } else if (shouldShiftIconDown(origin)){
            iconBounds.top = origin.y;
            iconBounds.bottom = origin.y + ICON_DIM;
        }

        return iconBounds;
    }

    private static boolean shouldShiftIconRight(Point origin){
        return origin.x - (ICON_DIM + ICON_OFFSET) < 0;
    }

    private static boolean shouldShiftIconDown(Point origin){
        return origin.y - (ICON_DIM + ICON_OFFSET) < 0;
    }
}