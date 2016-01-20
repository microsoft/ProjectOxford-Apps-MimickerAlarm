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

package com.microsoft.mimicker.mimics;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.widget.TextView;

import com.microsoft.mimicker.R;

/**
 * UI class to draw the top banner in Mimics to display the state of the game (failure, success,
 * timeout)
 *
 * animation is defined in game_success_animator.xml
 *
 */
public class MimicStateBanner extends TextView {
    private AnimatorSet mEnterLeftAnimation;
    private int mWidth;
    private int mSuccessColor, mFailureColor;

    public MimicStateBanner(Context context) {
        this(context, null);
    }

    public MimicStateBanner(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public MimicStateBanner(final Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mSuccessColor = ContextCompat.getColor(context, R.color.green3);
        mFailureColor = ContextCompat.getColor(context, R.color.dark3);

        mEnterLeftAnimation = (AnimatorSet) AnimatorInflater.loadAnimator(context,
                R.animator.game_success_animator);
        mEnterLeftAnimation.setTarget(this);
    }

    public void success(String message, final Command onAnimationEnd){
        setBackgroundColor(mSuccessColor);
        animate(message, onAnimationEnd);

    }

    public void failure(String message, final Command onAnimationEnd){
        setBackgroundColor(mFailureColor);
        animate(message, onAnimationEnd);
    }

    private void animate(String message, final Command onAnimationEnd){
        setText(message);
        mEnterLeftAnimation.removeAllListeners();
        mEnterLeftAnimation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                setVisibility(VISIBLE);
                bringToFront();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (onAnimationEnd != null) {
                    onAnimationEnd.execute();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });

        mEnterLeftAnimation.start();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
    }

    // This is required for animation
    public void setXPercentage(float value){
        value /= 100f;
        setX((mWidth > 0) ? (value * mWidth) : 0);
    }

    public interface Command {
        void execute();
    }
}
