package com.microsoft.mimicker.onboarding;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.widget.Button;

public class BubblePagerIndicator extends Button implements ViewPager.OnPageChangeListener{
    private Paint mPaint;
    private int mPosition;
    private int mTotalPositions;
    public BubblePagerIndicator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.FILL);
        mTotalPositions = 0;

        invalidate();
    }

    public BubblePagerIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BubblePagerIndicator(Context context) {
        this(context, null);
    }

    public void setTotalPositions(int totalPositions) {
        mTotalPositions = totalPositions;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int width = canvas.getWidth();
        int height = canvas.getHeight();
        if (mTotalPositions == 0){
            return;
        }
        mPaint.setAlpha(122);
        float buckets = (float) width / mTotalPositions;
        float radius = Math.min(buckets / 2f, height / 2f);
        for (int i = 0; i < mTotalPositions; i++) {
            canvas.drawCircle(i * buckets + buckets / 2f, height / 2f , radius, mPaint);
        }
        mPaint.setAlpha(255);
        canvas.drawCircle(mPosition * buckets + buckets / 2f, height / 2f, radius, mPaint);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        mPosition = position;
        invalidate();
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }
}

