package com.dj.tencentmap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.appcompat.widget.AppCompatSeekBar;


/**
 * 垂直SeekBar
 */
public class VerticalSeekBar extends AppCompatSeekBar {

    private Drawable mThumb;
    private OnSeekBarChangeListener mOnSeekBarChangeListener;
    private int degrees = -90;

    public VerticalSeekBar(Context context) {
        super(context);
    }

    public VerticalSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VerticalSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setOnSeekBarChangeListener(OnSeekBarChangeListener l) {
        mOnSeekBarChangeListener = l;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(h, w, oldh, oldw);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec);
        setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
    }

    @Override
    protected void onDraw(Canvas c) {
        c.rotate(degrees);
        c.translate(-getHeight(), 0);

        super.onDraw(c);
    }

    @Override
    public synchronized void setProgress(int progress) {
        super.setProgress(progress);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            onProgressRefresh((getProgress() - getMin()) / (float) (getMax() - getMin()), true);
        }else{
            onProgressRefresh(getProgress() / (float) getMax(), true);
        }
    }

    @Override
    public void setProgress(int progress, boolean animate) {
        super.setProgress(progress, animate);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            onProgressRefresh((getProgress() - getMin()) / (float) (getMax() - getMin()), true);
        }else{
            onProgressRefresh(getProgress() / (float) getMax(), true);
        }
    }

    public void onProgressRefresh(float scale, boolean fromUser) {
        Drawable thumb = mThumb;
        if (thumb != null) {
            setThumbPos(getHeight(), thumb, scale, Integer.MIN_VALUE);
            invalidate();
        }
        if (mOnSeekBarChangeListener != null) {
            mOnSeekBarChangeListener.onProgressChanged(this, getProgress(), fromUser);
        }
    }

    private void setThumbPos(int w, Drawable thumb, float scale, int gap) {
        int available = w - getPaddingLeft() - getPaddingRight();

        int thumbWidth = thumb.getIntrinsicWidth();
        int thumbHeight = thumb.getIntrinsicHeight();

        int thumbPos = (int) (scale * available + 0.5f);

        // int topBound = getWidth() / 2 - thumbHeight / 2 - getPaddingTop();
        // int bottomBound = getWidth() / 2 + thumbHeight / 2 - getPaddingTop();
        int topBound, bottomBound;
        if (gap == Integer.MIN_VALUE) {
            Rect oldBounds = thumb.getBounds();
            topBound = oldBounds.top;
            bottomBound = oldBounds.bottom;
        } else {
            topBound = gap;
            bottomBound = gap + thumbHeight;
        }
        thumb.setBounds(thumbPos, topBound, thumbPos + thumbWidth, bottomBound);
    }

    @Override
    public void setThumb(Drawable thumb) {
        mThumb = rotateDrawable(thumb,degrees);
        super.setThumb(mThumb);
    }

    private Drawable rotateDrawable(Drawable drawable, float angle){
        //创建一个Matrix对象
        Matrix matrix = new Matrix();
        //由darwable创建一个bitmap对象
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        //设置旋转角度
        matrix.setRotate(angle);
        //以bitmap跟matrix一起创建一个新的旋转以后的bitmap
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                bitmap.getHeight(), matrix, true);
        //bitmap转化为drawable对象
        //不要使用new BitmapDrawable(Bitmap bitmap)，使用下面这个，可以正确设置其目标的密度。
        return new BitmapDrawable(getResources(),bitmap);
    }

    void onStartTrackingTouch() {
        if (mOnSeekBarChangeListener != null) {
            mOnSeekBarChangeListener.onStartTrackingTouch(this);
        }
    }

    void onStopTrackingTouch() {
        if (mOnSeekBarChangeListener != null) {
            mOnSeekBarChangeListener.onStopTrackingTouch(this);
        }
    }

    private void attemptClaimDrag() {
        if (getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                setPressed(true);
                onStartTrackingTouch();
                if (!getThumb().getBounds().contains(getHeight() - (int) event.getY(), (int) event.getX())) {
                    setProgress(getMax() - (int) (getMax() * event.getY() / getHeight()));
                }
                break;

            case MotionEvent.ACTION_MOVE:
                attemptClaimDrag();
                if (!getThumb().getBounds().contains(getHeight() - (int) event.getY(), (int) event.getX())) {
                    setProgress(getMax() - (int) (getMax() * event.getY() / getHeight()));
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                onStopTrackingTouch();
                setPressed(false);
                break;
            default:
        }
        return true;
    }
}