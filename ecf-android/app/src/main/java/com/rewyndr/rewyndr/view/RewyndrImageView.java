package com.rewyndr.rewyndr.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.rewyndr.rewyndr.R;
import com.rewyndr.rewyndr.boundary.ImageProcessor;
import com.rewyndr.rewyndr.boundary.Segmentation;
import com.rewyndr.rewyndr.enums.RewyndrImageState;
import com.rewyndr.rewyndr.interfaces.IRewyndrImageObserver;
import com.rewyndr.rewyndr.model.Image;
import com.rewyndr.rewyndr.model.Tag;

import java.util.ArrayList;
import java.util.List;

public class RewyndrImageView extends androidx.appcompat.widget.AppCompatImageView {

    public Image image;

    private Tag selectedTag;

    private ArrayList<IRewyndrImageObserver> observers = new ArrayList<IRewyndrImageObserver>();

    public enum TagOperationMode {
        add,
        edit,
        view
    }

    private TagOperationMode currentTagMode = TagOperationMode.view;

    private RewyndrImageState annotationTagMode;

    // We use these properties to hold a representation of each tag type from
    // which (1) a list of points and (2) a bitmap can be derived
    private RectF boxTagLocation = null;
    private List<Point> smartTagLocation = null;
    private List<Point> lineTagLocation = null;

    private GestureDetector addTagGestureDetector;
    private GestureDetector editLineTagGestureDetector;
    private Segmentation segmentation;
    private int currentThreshold = 10;
    private int maxThreshold = 100;

    public boolean zoomEnabled;
    private ScaleGestureDetector mScaleDetector;
    private float mScaleFactor = 1.f;

    public RewyndrImageView(Context context) {
        super(context);
        init(context);
    }

    public RewyndrImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RewyndrImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context){
        addTagGestureDetector = new GestureDetector(context, addTagGestureListener);
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Log.d("[SCALING]", "Drawing with scale: " + mScaleFactor);

        canvas.save();
        canvas.scale(mScaleFactor, mScaleFactor);

        canvas.restore();
    }

    public void setImage(String url){
        setImage(new Image(url));
    }

    public void setImage(Image image){
        if(image == null)
            return;

        this.image = image;
        Glide.with(getContext()).load(image.getUrl())
                .placeholder(R.drawable.ic_image_black_24dp).into(imageTarget);
    }

    private CustomTarget imageTarget = new CustomTarget<BitmapDrawable>() {
        @Override
        public void onResourceReady(BitmapDrawable bitmap, Transition<? super BitmapDrawable> transition) {
            image.setLargeBitmap(bitmap.getBitmap());
            RewyndrImageView.this.setImageDrawable(bitmap);

            // Generate the highlighted tag bitmap
            Bitmap newBitmap = ImageProcessor
                    .generateHighlightedTaggedBitmap(
                            getContext(),
                            image.getLargeBitmap(),
                            image.getTaggedLargeBitmap(getContext()),
                            image.getDarkenTaggedLargeBitmap(getContext()),
                            image.getTags(), -100, -100);

            RewyndrImageView.this.setImageBitmap(newBitmap);
            notifyImageChangedListeners(newBitmap);

            RewyndrImageView.this.setOnTouchListener(onPhotoTouchListener);
        }

        public void onLoadCleared(Drawable d){}
    };

    private float getPhotoVerticalScaleFactor(){
        Bitmap largeBitmap = image.getLargeBitmap();
        return (float) this.getHeight()/largeBitmap.getHeight();
    }

    private float getPhotoHorizontalScaleFactor(){
        Bitmap largeBitmap = image.getLargeBitmap();
        return (float) this.getWidth()/largeBitmap.getWidth();
    }

    private float getPhotoOffsetX() {
        Bitmap largeBitmap = image.getLargeBitmap();
        boolean isExpandHorizontal = ((float) largeBitmap.getHeight() / largeBitmap.getWidth()) > ((float) this.getHeight() / this.getWidth());

        return isExpandHorizontal ? ((this.getWidth() - getPhotoHorizontalScaleFactor() * largeBitmap.getWidth()) / 2) * -1 : 0;
    }

    private float getPhotoOffsetY() {
        Bitmap largeBitmap = image.getLargeBitmap();
        boolean isExpandHorizontal = ((float) largeBitmap.getHeight() / largeBitmap.getWidth()) > ((float) this.getHeight() / this.getWidth());

        return isExpandHorizontal ? 0 : ((this.getHeight() - getPhotoVerticalScaleFactor() * largeBitmap.getHeight()) / 2) * -1;
    }

    public void setTaggingOperationMode(TagOperationMode mode){
        if(image == null || mode == currentTagMode)
            return;

        currentTagMode = mode;

        setOnTouchListener(null);

        Bitmap newBitmap = null;

        switch(currentTagMode){
            case add:
                newBitmap = image.getLargeBitmap();
                setOnTouchListener(onAddTagTouchListener);
                annotationTagMode = RewyndrImageState.NEW;

                notifyTagBoundsListeners();
                break;
            case edit:
                switch(selectedTag.getBoundaryType()) {
                    case "box":
                        annotationTagMode = RewyndrImageState.EDIT_BOX;
                        boxTagLocation = selectedTag.rectangle();
                        setOnTouchListener(onEditTagTouchListener);

                        newBitmap = ImageProcessor.drawEditSquare(
                            image.getLargeBitmap(),
                            ImageProcessor.generateDarkenImage(image.getLargeBitmap(), 100),
                            boxTagLocation,
                            true
                        );
                        break;
                    case "smart":
                        annotationTagMode = RewyndrImageState.EDIT_SMART;
                        smartTagLocation = selectedTag.getPoints();
                        segmentation = new Segmentation(image.getLargeBitmap());
                        setOnTouchListener(onSegmentationTouchListener);
                        break;
                }
                notifyTagBoundsListeners();
                break;
            case view:
            default:
                selectedTag = null;
                boxTagLocation = null;
                segmentation = null;
                annotationTagMode = RewyndrImageState.NONE;

                for(IRewyndrImageObserver obs : observers){
                    obs.tagUpdate(selectedTag);
                }

                newBitmap = image.getTaggedLargeBitmap(getContext());
                setOnTouchListener(onPhotoTouchListener);
        }

        if(newBitmap != null){
            setImageBitmap(newBitmap);
            notifyImageChangedListeners(newBitmap);
        }

        for(IRewyndrImageObserver obs : observers){
            obs.stateUpdate(annotationTagMode);
        }
    }

    private List<Point> getBoundaryPoints() {
        List<Point> boundaryPoints = new ArrayList<Point> ();
        switch(annotationTagMode){
            case EDIT_BOX:
                if(boxTagLocation == null)
                    break;

                boundaryPoints = new ArrayList<Point>() {{
                    add(new Point((int) boxTagLocation.left, (int) boxTagLocation.top));
                    add(new Point((int) boxTagLocation.right, (int) boxTagLocation.top));
                    add(new Point((int) boxTagLocation.right, (int) boxTagLocation.bottom));
                    add(new Point((int) boxTagLocation.left, (int) boxTagLocation.bottom));
                }};
                break;
            case EDIT_SMART:
                if(smartTagLocation == null)
                    break;

                boundaryPoints = smartTagLocation;
        }

        return boundaryPoints;
    }

    private void notifyTagBoundsListeners(){
        List<Point> points = getBoundaryPoints();
        for(IRewyndrImageObserver observer : observers){
            observer.tagBoundsUpdated(points);
        }
    }

    private void notifyImageChangedListeners(Bitmap newBitmap){
        for(IRewyndrImageObserver observer : observers){
            observer.imageChanged(newBitmap);
        }
    }

    private View.OnTouchListener onPhotoTouchListener = new View.OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
            float bitmapX = bitmapX(event);
            float bitmapY = bitmapY(event);

            if(zoomEnabled)
                mScaleDetector.onTouchEvent(event);

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // Generate the highlighted tag bitmap
                    Bitmap newBitmap = ImageProcessor
                            .generateHighlightedTaggedBitmap(
                                    getContext(),
                                    image.getLargeBitmap(),
                                    image.getTaggedLargeBitmap(getContext()),
                                    image.getDarkenTaggedLargeBitmap(getContext()),
                                    image.getTags(), bitmapX, bitmapY);

                    // Set to the current image view
                    RewyndrImageView.this.setImageBitmap(newBitmap);
                    notifyImageChangedListeners(newBitmap);

                    selectedTag = ImageProcessor.getSelectedTag(
                            image.getTags(), bitmapX, bitmapY);

                    for(IRewyndrImageObserver obs : observers){
                        obs.tagUpdate(selectedTag);
                    }
                    break;
                default:
                    break;
            }
            return true;
        }
    };

    private View.OnTouchListener onAddTagTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            RewyndrImageView.this.addTagGestureDetector.onTouchEvent(event);
            return true;
        }
    };

    private View.OnTouchListener onEditTagTouchListener = new View.OnTouchListener() {
        float prevBitmapX = 0;
        float prevBitmapY = 0;
        int movingNode = 8;

        public boolean onTouch(View v, MotionEvent event) {
            float bitmapX = bitmapX(event);
            float bitmapY = bitmapY(event);

            //scrollView.setScrollingEnabled(false); //TODO: Figure out why this is here

            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_MOVE:
                    // During move, keep updating the edit square
                    // Change tag location based on touch location
                    changeTagLocation(prevBitmapX, prevBitmapY, bitmapX, bitmapY, movingNode);

                    // Draw new edit tag square
                    Bitmap newBitmap = ImageProcessor.drawEditSquare(
                            image.getLargeBitmap(),
                            ImageProcessor.generateDarkenImage(image.getLargeBitmap(), 100),
                            getValidTagLocation(boxTagLocation), true);

                    RewyndrImageView.this.setImageBitmap(newBitmap);
                    notifyImageChangedListeners(newBitmap);

                    // Save touch location
                    prevBitmapX = bitmapX;
                    prevBitmapY = bitmapY;
                    break;
                case MotionEvent.ACTION_DOWN:
                    movingNode = determineMovingMode(bitmapX, bitmapY);
                    break;
                case MotionEvent.ACTION_UP:
                    // Clear last touch location
                    prevBitmapX = -1;
                    prevBitmapY = -1;
                    boxTagLocation = getValidTagLocation(boxTagLocation);
                    //scrollView.setScrollingEnabled(true); //TODO: Figure out why this is here
                    break;
                default:
                    break;
            }

            notifyTagBoundsListeners();
            return true;
        }
    };

    private View.OnTouchListener onSegmentationTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    if(!segmentation.hasBeenTouched()) {
                        segmentation.setTouchLocation((int) bitmapX(event), (int) bitmapY(event));
                    }

                    // Add threshold
                    // TODO: Look into decreasing threshold here for more fine-grained boundary refinement
                    currentThreshold = (currentThreshold + 10) % maxThreshold;

                    // Segmentation
                    segmentation.segmentation(currentThreshold);
                    Bitmap newBitmap = segmentation.getResultBitmap();
                    RewyndrImageView.this.setImageBitmap(newBitmap);
                    notifyImageChangedListeners(newBitmap);
                    smartTagLocation = segmentation.getConvexHullPointList();

                    notifyTagBoundsListeners();
                default:
                    break;
            }
            return true;
        }
    };

    private GestureDetector.OnGestureListener addTagGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public void onLongPress(MotionEvent event) {
            annotationTagMode = RewyndrImageState.EDIT_SMART;

            for(IRewyndrImageObserver obs : observers){
                obs.stateUpdate(annotationTagMode);
            }

            // Segmentation
            segmentation = new Segmentation(image.getLargeBitmap());
            segmentation.setTouchLocation((int) bitmapX(event), (int) bitmapY(event));
            segmentation.segmentation(currentThreshold);
            smartTagLocation = segmentation.getConvexHullPointList();
            Bitmap newBitmap = segmentation.getResultBitmap();
            RewyndrImageView.this.setImageBitmap(newBitmap);
            notifyImageChangedListeners(newBitmap);

            // Set following on touch listener
            RewyndrImageView.this.setOnTouchListener(null);
            RewyndrImageView.this.setOnTouchListener(onSegmentationTouchListener);

            notifyTagBoundsListeners();
        }

        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            annotationTagMode = RewyndrImageState.EDIT_BOX;

            for(IRewyndrImageObserver obs : observers){
                obs.stateUpdate(annotationTagMode);
            }

            float bitmapX = bitmapX(event);
            float bitmapY = bitmapY(event);

            float left = Math.max(10, bitmapX);
            float top = Math.max(10, bitmapY);
            float right = left + 100;
            float bottom = top + 100;

            boxTagLocation = new RectF(left, top, right, bottom);

            // Draw initial tag square
            Bitmap initialBitmap = ImageProcessor.drawEditSquare(
                    image.getLargeBitmap(),
                    ImageProcessor.generateDarkenImage(image.getLargeBitmap(), 100),
                    boxTagLocation,
                    true
            );

            RewyndrImageView.this.setImageBitmap(initialBitmap);
            notifyImageChangedListeners(initialBitmap);

            // Set following on touch listener
            RewyndrImageView.this.setOnTouchListener(null);
            RewyndrImageView.this.setOnTouchListener(onEditTagTouchListener);

            notifyTagBoundsListeners();
            return true;
        }
    };

    private boolean isInBoundary(float x, float y) {
        float leftBoundary = ImageProcessor.IMAGE_BORDER_WIDTH;
        float topBoundary = ImageProcessor.IMAGE_BORDER_WIDTH;
        float rightBoundary = image.getLargeBitmap().getWidth() - ImageProcessor.IMAGE_BORDER_WIDTH;
        float bottomBoundary = image.getLargeBitmap().getHeight() - ImageProcessor.IMAGE_BORDER_WIDTH;

        return x >= leftBoundary && x <= rightBoundary && y >= topBoundary && y <= bottomBoundary;
    }

    private RectF getValidTagLocation(RectF tagLocation) {
        float left = tagLocation.left;
        float right = tagLocation.right;
        float top = tagLocation.top;
        float bottom = tagLocation.bottom;
        float temp = 0;
        if (left > right) {
            temp = left;
            left = right;
            right = temp;
        }
        if (top > bottom) {
            temp = top;
            top = bottom;
            bottom = temp;
        }
        return new RectF(left, top, right, bottom);
    }

    // This function test whether user wants to move the tag or resize the tag
    // based on touch location.
    private int determineMovingMode(float curX, float curY) {
        int touchRadius = 50;

        // Calculate whether the touch location is in the edit square region
        float left = boxTagLocation.left;
        float right = boxTagLocation.right;
        float top = boxTagLocation.top;
        float bottom = boxTagLocation.bottom;
        float[] editSquareXs = { left, (left + right) / 2, right, left, right,
                left, (left + right) / 2, right };
        float[] editSquareYs = { top, top, top, (top + bottom) / 2,
                (top + bottom) / 2, bottom, bottom, bottom };

        for (int i = 0; i <= 7; i++) {
            boolean inSquare = Math.abs(curX - editSquareXs[i]) <= touchRadius
                    && Math.abs(curY - editSquareYs[i]) <= touchRadius;
            if (inSquare) {
                return i;
            }
        }
        // Not in any edit square region, then default is 8, move the tag
        return 8;
    }

    // This function is used to change tag location based on the user
    // touch location
    // All location is location on the bitmap, not on the enlarged bitmap
    private void changeTagLocation(float prevX, float prevY, float curX,
                                   float curY, int movingMode) {
        // Fist touch situation, when prevX and prevY are less than 0
        if (prevX < 0 && prevY < 0) {
            return;
        }

        float left = boxTagLocation.left;
        float right = boxTagLocation.right;
        float top = boxTagLocation.top;
        float bottom = boxTagLocation.bottom;
        float offsetX = curX - prevX;
        float offsetY = curY - prevY;

        // Guard against out-of-bounds tag.
        if(!isInBoundary(curX, curY)) {
            return;
        }

        // Change the tag location
        if (movingMode == 0) {
            // Change leftTop
            boxTagLocation.set(curX, curY, right, bottom);
        } else if (movingMode == 1) {
            // Change middleTop
            boxTagLocation.set(left, curY, right, bottom);
        } else if (movingMode == 2) {
            // Change rightTop
            boxTagLocation.set(left, curY, curX, bottom);
        } else if (movingMode == 3) {
            // Change leftMiddle
            boxTagLocation.set(curX, top, right, bottom);
        } else if (movingMode == 4) {
            // Change rightMiddle
            boxTagLocation.set(left, top, curX, bottom);
        } else if (movingMode == 5) {
            // Change leftBottom
            boxTagLocation.set(curX, top, right, curY);
        } else if (movingMode == 6) {
            // Change middleBottom
            boxTagLocation.set(left, top, right, curY);
        } else if (movingMode == 7) {
            // Change bottomRight
            boxTagLocation.set(left, top, curX, curY);
        } else {
            boxTagLocation.set(left + offsetX, top + offsetY, right + offsetX, bottom + offsetY);
        }
    }

    private class ScaleListener
            extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();

            Log.d("[SCALING]", "Setting scale: " + mScaleFactor);

            RewyndrImageView.this.invalidate();
            return true;
        }
    }

    private float bitmapX(MotionEvent event) {
        return (event.getX() + getPhotoOffsetX()) / getPhotoHorizontalScaleFactor();
    }

    private float bitmapY(MotionEvent event) {
        return (event.getY() + getPhotoOffsetY()) / getPhotoVerticalScaleFactor();
    }

    public void registerObserver(IRewyndrImageObserver obs){
        observers.add(obs);
    }

    //TODO: This probably isn't the right way to do this
    public void unregisterObserver(IRewyndrImageObserver obs){
        observers.remove(obs);
    }
}
