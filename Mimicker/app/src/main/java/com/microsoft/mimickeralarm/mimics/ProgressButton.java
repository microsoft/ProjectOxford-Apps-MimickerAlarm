/*
 *
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license.
 *
 * Project Oxford: http://ProjectOxford.ai
 *
 * Project Oxford Mimicker Alarm Github:
 * https://github.com/Microsoft/ProjectOxford-Apps-MimickerAlarm
 *
 * Copyright (c) Microsoft Corporation
 * All rights reserved.
 *
 * MIT License:
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED ""AS IS"", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package com.microsoft.mimickeralarm.mimics;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import com.microsoft.mimickeralarm.R;

/**
 * Simple UI class that implements a button with states
 *
 * The states are :
 * capture image,
 * capture audio,
 * stop capturing audio,
 * processing
 **/
public class ProgressButton extends ImageView {
    private static final int PRESSED_ANIMATION_DURATION = 200;
    private static final float PRESSED_ANIMATION_GROW_FACTOR = 1.2f;
    private static final int LOADING_ANIMATION_DURATION = 2000;
    private static final long INTERACTION_HINT_ANIMATION_DURATION = 1000;
    private static final float INTERACTION_HINT_GROW_FACTOR = 1.1f;
    private static int sYellow, sBlue, sGrey, sWhite;
    private State mState;
    private State mReadyState;
    private Paint mBrush;
    private float mRadius;
    private int mCenterX, mCenterY;
    private float mInitialRadius;
    private Bitmap mMicrophoneIcon;
    private Bitmap mCameraIcon;
    private ObjectAnimator mPressedAnimation;
    private ObjectAnimator mLoadingAnimation;
    // A growing/shrinking animation of the button border to give the hint to the user to press the button
    private ObjectAnimator mInteractionHintAnimation;
    private float mLoadingAnimationProgress;
    private RectF mLoadingAnimationRect;
    public ProgressButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setFocusable(true);
        setScaleType(ScaleType.CENTER_INSIDE);

        sYellow = ContextCompat.getColor(context, R.color.yellow3);
        sBlue = ContextCompat.getColor(context, R.color.blue);
        sGrey = ContextCompat.getColor(context, R.color.grey);
        sWhite = ContextCompat.getColor(context, R.color.white);
        mMicrophoneIcon = ((BitmapDrawable)ContextCompat.getDrawable(context, android.R.drawable.ic_btn_speak_now)).getBitmap();
        mCameraIcon = ((BitmapDrawable)ContextCompat.getDrawable(context, android.R.drawable.ic_menu_camera)).getBitmap();

        mBrush = new Paint(Paint.ANTI_ALIAS_FLAG);

        mLoadingAnimation = ObjectAnimator.ofFloat(this, "loadingAnimationProgress", 0f, 360f);
        mLoadingAnimation.setDuration(LOADING_ANIMATION_DURATION);
        mLoadingAnimation.setRepeatCount(ObjectAnimator.INFINITE);

        mPressedAnimation = ObjectAnimator.ofFloat(this, "radius", 0f, 0f);
        mPressedAnimation.setDuration(PRESSED_ANIMATION_DURATION);
        mPressedAnimation.setInterpolator(new DecelerateInterpolator());
    }
    public ProgressButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public ProgressButton(Context context) {
        this(context, null);
    }

    // These setters and getters are required by animator
    public float getRadius() {
        return mRadius;
    }

    public void setRadius(float radius) {
        this.mRadius = radius;
        this.invalidate();
    }

    public float getLoadingAnimationProgress() {
        return mLoadingAnimationProgress;
    }

    public void setLoadingAnimationProgress(float loadingAnimationProgress) {
        this.mLoadingAnimationProgress = loadingAnimationProgress;
        this.invalidate();
    }

    private void prepareDrawText(int color) {
        mBrush.setFlags(Paint.LINEAR_TEXT_FLAG);
        mBrush.setStyle(Paint.Style.STROKE);
        mBrush.setColor(color);
        mBrush.setTextAlign(Paint.Align.CENTER);
        mBrush.setStrokeWidth(0f);
        mBrush.setTextSize(mInitialRadius);
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
            mPressedAnimation.setFloatValues(mRadius, mInitialRadius * PRESSED_ANIMATION_GROW_FACTOR);
            mPressedAnimation.start();
        }
        else{
            mPressedAnimation.setFloatValues(mRadius, mInitialRadius);
            mPressedAnimation.start();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mState == State.ReadyCamera) {
            prepareDrawFill(sYellow);
            canvas.drawCircle(mCenterX, mCenterY, mRadius, mBrush);

            canvas.drawBitmap(mCameraIcon,
                    mCenterX - (mCameraIcon.getWidth() / 2),
                    mCenterY - (mCameraIcon.getHeight() / 2),
                    mBrush);
        }
        else if (mState == State.ReadyAudio) {
            prepareDrawFill(sYellow);
            canvas.drawCircle(mCenterX, mCenterY, mRadius, mBrush);

            canvas.drawBitmap(mMicrophoneIcon,
                    mCenterX - (mMicrophoneIcon.getWidth() / 2),
                    mCenterY - (mMicrophoneIcon.getHeight() / 2),
                    mBrush);
        }
        else if (mState == State.Loading) {
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
        mInitialRadius = Math.min(w, h) / 2 - getContext().getResources()
                .getDimensionPixelSize(R.dimen.progress_button_padding);
        mRadius = mInitialRadius;
        prepareDrawText(sWhite);

        float radius = mInitialRadius / 2f;
        mLoadingAnimationRect = new RectF(mCenterX - radius, mCenterY - radius, mCenterX + radius, mCenterY + radius);

        mInteractionHintAnimation = ObjectAnimator.ofFloat(this, "radius", mInitialRadius, mInitialRadius * INTERACTION_HINT_GROW_FACTOR, mInitialRadius);
        mInteractionHintAnimation.setDuration(INTERACTION_HINT_ANIMATION_DURATION);
        mInteractionHintAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        mInteractionHintAnimation.setRepeatCount(ValueAnimator.INFINITE);
        mInteractionHintAnimation.start();
    }

    public void setReady() {
        mState = mReadyState;
        setClickable(true);
        stop();
        if (mInteractionHintAnimation != null) {
            // When the game starts, setReady() will be called before onSizeChanged() is called.
            // However, we will not have mInitialRadius set until OnSizeChanged() is called.
            // So we will start animation in OnSizeChanged().
            // However, if the user fails the mimics, setReady() will be called for several times.
            // In that case, we will re-start the animation here.
            mInteractionHintAnimation.start();
        }
        invalidate();
    }

    public void setReadyState(State state) {
        mReadyState = state;
    }

    public Boolean isReady() {
        return mState == State.ReadyCamera || mState == State.ReadyAudio;
    }

    public void waiting() {
        mState = State.Waiting;
        mInteractionHintAnimation.cancel();
        invalidate();
    }

    public void loading() {
        mState = State.Loading;
        setClickable(false);
        mInteractionHintAnimation.cancel();
        mLoadingAnimation.start();
    }

    public void stop() {
        mLoadingAnimation.cancel();
        mPressedAnimation.cancel();
        if (mInteractionHintAnimation != null) {
            mInteractionHintAnimation.cancel();
        }
    }

    public enum State {
        ReadyCamera,
        ReadyAudio,
        Loading,
        Waiting,
    }
}

