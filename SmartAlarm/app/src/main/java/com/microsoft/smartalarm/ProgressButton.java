package com.microsoft.smartalarm;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

public class ProgressButton extends ImageView {
    private enum State{
        Ready,
        Loading,
        Waiting,
    }

    private State mState;
    private Paint mBrush;
    private float mRadius;
    private int mCenterX, mCenterY;
    private static float sRadius;

    private final static int sRed = Color.parseColor("#F44336");

    private final static int sBlue = Color.parseColor("#3F51B5");
    private final static int sGrey = Color.parseColor("#707070");
    private final static int sWhite = Color.parseColor("#ffffff");


    private ObjectAnimator mPressedAnimation;
    private static final int sPressedAnimationDuration = 200;
    private static final float sPressedAnimationSize = 1.2f;
    public float getRadius() {
        return mRadius;
    }
    public void setRadius(float radius) {
        this.mRadius = radius;
        this.invalidate();
    }

    private ObjectAnimator mLoadingAnimation;
    private static final int sLoadingAnimationDuration = 2000;
    private float mLoadingAnimationProgress;
    public float getLoadingAnimationProgress() {
        return mLoadingAnimationProgress;
    }
    public void setLoadingAnimationProgress(float loadingAnimationProgress) {
        this.mLoadingAnimationProgress = loadingAnimationProgress;
        this.invalidate();
    }
    private RectF mLoadingAnimationRect;

    public ProgressButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setFocusable(true);
        setScaleType(ScaleType.CENTER_INSIDE);

        mBrush = new Paint(Paint.ANTI_ALIAS_FLAG);

        mLoadingAnimation = ObjectAnimator.ofFloat(this, "loadingAnimationProgress", 0f, 360f);
        mLoadingAnimation.setDuration(sLoadingAnimationDuration);
        mLoadingAnimation.setRepeatCount(ObjectAnimator.INFINITE);

        mPressedAnimation = ObjectAnimator.ofFloat(this, "radius", 0f, 0f);
        mPressedAnimation.setDuration(sPressedAnimationDuration);
        mPressedAnimation.setInterpolator(new DecelerateInterpolator());
        ready();
    }

    public ProgressButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProgressButton(Context context) {
        this(context, null);
    }

    private void prepareDrawText(int color) {
        mBrush.setFlags(Paint.LINEAR_TEXT_FLAG);
        mBrush.setStyle(Paint.Style.STROKE);
        mBrush.setColor(color);
        mBrush.setTextAlign(Paint.Align.CENTER);
        mBrush.setStrokeWidth(0f);
        mBrush.setTextSize(sRadius);
    }

    private void prepareDrawFill(int color) {
        mBrush.setFlags(Paint.ANTI_ALIAS_FLAG);
        mBrush.setStyle(Paint.Style.FILL);
        mBrush.setColor(color);
    }

    private void prepareDrawStroke(int color) {
        mBrush.setFlags(Paint.ANTI_ALIAS_FLAG);
        mBrush.setStyle(Paint.Style.STROKE);
        mBrush.setStrokeWidth(10f);
        mBrush.setColor(color);
    }

    @Override
    public void setPressed(boolean pressed) {
        super.setPressed(pressed);
        if (pressed) {
            mPressedAnimation.setFloatValues(mRadius, sRadius * sPressedAnimationSize);
            mPressedAnimation.start();
        }
        else{
            mPressedAnimation.setFloatValues(mRadius, sRadius);
            mPressedAnimation.start();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mState == State.Ready) {
            prepareDrawFill(sRed);
            canvas.drawCircle(mCenterX, mCenterY, mRadius, mBrush);

            int xPos = (canvas.getWidth() / 2);
            prepareDrawText(sWhite);
            int yPos = (int) ((canvas.getHeight() / 2) - ((mBrush.descent() + mBrush.ascent()) / 2)) ;
            canvas.drawText("R", xPos, yPos, mBrush);
        } else if (mState == State.Loading) {
            prepareDrawFill(sBlue);
            canvas.drawCircle(mCenterX, mCenterY, mRadius, mBrush);

            prepareDrawStroke(sWhite);
            canvas.drawArc(mLoadingAnimationRect, mLoadingAnimationProgress, 300f, false, mBrush);
        }
        else if (mState == State.Waiting) {
            prepareDrawFill(sGrey);
            canvas.drawCircle(mCenterX, mCenterY, mRadius, mBrush);
            prepareDrawFill(sWhite);
            canvas.drawCircle(mCenterX, mCenterY, (float) (mRadius * 0.7), mBrush);
            prepareDrawFill(sGrey);
            float w = (float)(mRadius * 0.3);
            canvas.drawRect(mCenterX - w, mCenterY - w, mCenterX + w, mCenterY + w, mBrush);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mCenterX = w / 2;
        mCenterY = h / 2;
        sRadius = Math.min(w, h) / 2 - 20;
        mRadius = sRadius;
        prepareDrawText(sWhite);

        float radius = sRadius / 2f;
        mLoadingAnimationRect = new RectF(mCenterX - radius, mCenterY - radius, mCenterX + radius, mCenterY + radius);
    }

    public void ready() {
        mState = State.Ready;
        setClickable(true);
        stop();
        invalidate();
    }
    public Boolean isReady() {
        return mState == State.Ready;
    }

    public void waiting() {
        mState = State.Waiting;
        invalidate();
    }

    public void loading() {
        mState = State.Loading;
        setClickable(false);

        mLoadingAnimation.start();
    }
    public void stop() {
        mLoadingAnimation.cancel();
        mPressedAnimation.cancel();
    }
}

