package com.microsoft.smartalarm;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.view.View;

public class CountDownTimerView extends View {
    public interface Command{
        void execute();
    }

    private long mTotalTime = 0;
    private Command mCommand = null;
    private CountDownTimer mTimer = null;
    private int mWidth;

    private RectF mProgressBar = null;
    private Paint mPaint;

    private final static int sInterval = 100;
    private final static int sHeight = 10;

    public CountDownTimerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.RED);
        mProgressBar = new RectF(0, 0, 0, sHeight);
    }

    public CountDownTimerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CountDownTimerView(Context context) {
        this(context, null);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(mProgressBar, mPaint);
    }

    public void start() {
        if (mTimer != null) {
            mTimer.start();
        }
    }

    public void init(int time, Command doOnTimeout) {
        mTotalTime = time;
        mCommand = doOnTimeout;
        mTimer = new CountDownTimer(time, sInterval) {
            @Override
            public void onTick(long millisUntilFinished) {
                mProgressBar.set(0, 0, mWidth * ((float)millisUntilFinished / (float)mTotalTime), sHeight);
                invalidate();
            }
            @Override
            public void onFinish() {
                if (mCommand != null) {
                    mProgressBar.set(0, 0, 0, sHeight);
                    invalidate();
                    mCommand.execute();
                }
            }
        };
    }
}

