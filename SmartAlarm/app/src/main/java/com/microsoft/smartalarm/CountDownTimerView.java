package com.microsoft.smartalarm;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.CountDownTimer;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

public class CountDownTimerView extends View {
    public interface Command{
        void execute();
    }

    private long mTotalTime = 0;
    private Command mCommand = null;
    private CountDownTimer mTimer = null;
    private int mWidth, mHeight;
    private long mMillisUntilFinished;
    private Boolean mIsPaused = false;

    private Paint m25PercentPaint, m50PercentPaint, m75PercentPaint, m100PercentPaint;
    private RectF m25PercentRect, m50PercentRect, m75PercentRect, m100PercentRect;

    private final static int sInterval = 100;

    public CountDownTimerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        ContextCompat contextCompat = new ContextCompat();
        m25PercentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        m25PercentPaint.setColor(contextCompat.getColor(context, R.color.yellow4));
        m50PercentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        m50PercentPaint.setColor(contextCompat.getColor(context, R.color.yellow3));
        m75PercentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        m75PercentPaint.setColor(contextCompat.getColor(context, R.color.yellow2));
        m100PercentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        m100PercentPaint.setColor(contextCompat.getColor(context, R.color.yellow1));

        m25PercentRect = new RectF(0, 0, 0, 0);
        m50PercentRect = new RectF(0, 0, 0, 0);
        m75PercentRect = new RectF(0, 0, 0, 0);
        m100PercentRect = new RectF(0, 0, 0, 0);
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
        mHeight = MeasureSpec.getSize(widthMeasureSpec);

        m25PercentRect.set(0, 0, mWidth / 4f, mHeight);
        m50PercentRect.set(mWidth / 4f, 0, 2 * mWidth / 4f, mHeight);
        m75PercentRect.set(2 * mWidth / 4f, 0, 3 * mWidth / 4f, mHeight);
        m100PercentRect.set(3 * mWidth / 4f, 0, mWidth, mHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float millisRemaining = (float)mMillisUntilFinished;
        float percentage = millisRemaining / (float) mTotalTime;
        float width;
        if (percentage > 0) {
            width = percentage > 0.25 ? 0.25f * mWidth : percentage * mWidth;
            percentage = percentage - 0.25f;
            m25PercentRect.set(m25PercentRect.left, m25PercentRect.top, width,m25PercentRect.bottom);
            canvas.drawRect(m25PercentRect, m25PercentPaint);
        }
        if (percentage > 0) {
            width = percentage > 0.25 ? 0.25f * mWidth : percentage * mWidth;
            percentage = percentage - 0.25f;
            m50PercentRect.set(m50PercentRect.left, m50PercentRect.top, m50PercentRect.left + width,m50PercentRect.bottom);
            canvas.drawRect(m50PercentRect, m50PercentPaint);
        }
        if (percentage > 0) {
            width = percentage > 0.25 ? 0.25f * mWidth : percentage * mWidth;
            percentage = percentage - 0.25f;
            m75PercentRect.set(m75PercentRect.left, m75PercentRect.top, m75PercentRect.left + width,m75PercentRect.bottom);
            canvas.drawRect(m75PercentRect, m75PercentPaint);
        }
        if (percentage > 0) {
            width = percentage > 0.25 ? 0.25f * mWidth : percentage * mWidth;
            percentage = percentage - 0.25f;
            m100PercentRect.set(m100PercentRect.left, m100PercentRect.top, m100PercentRect.left + width,m100PercentRect.bottom);
            canvas.drawRect(m100PercentRect, m100PercentPaint);
        }
    }

    public void start() {
        if (mTimer == null) {
            createNewTimer(mTotalTime);
        }
        mTimer.start();
    }

    public void stop() {
        mTimer.cancel();
    }

    public void pause() {
        stop();
        mIsPaused = true;
    }

    public void resume() {
        if (mIsPaused) {
            createNewTimer(mMillisUntilFinished);
            mTimer.start();
        }
    }

    private void createNewTimer(long millisUntilFinished) {
        mTimer = new CountDownTimer(millisUntilFinished, sInterval) {
            @Override
            public void onTick(long millisUntilFinished) {
                mMillisUntilFinished = millisUntilFinished;
                invalidate();
            }
            @Override
            public void onFinish() {
                if (mCommand != null) {
                    mMillisUntilFinished = 0;
                    invalidate();
                    mCommand.execute();
                }
            }
        };
    }

    public void init(int time, Command doOnTimeout) {
        mTotalTime = time;
        mCommand = doOnTimeout;
    }
}

