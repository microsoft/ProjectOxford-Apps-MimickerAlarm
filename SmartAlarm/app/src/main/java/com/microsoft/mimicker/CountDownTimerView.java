package com.microsoft.mimicker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.CountDownTimer;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

public class CountDownTimerView extends View {
    private final static int sInterval = 100;
    private long mTotalTime = 0;
    private Command mCommand = null;
    private CountDownTimer mTimer = null;
    private int mWidth, mHeight;
    private long mMillisUntilFinished;
    private Boolean mIsPaused = false;

    private Paint m25PercentPaint, m50PercentPaint, m75PercentPaint, m100PercentPaint;
    private Paint mWhitePaint;

    public CountDownTimerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        m25PercentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        m25PercentPaint.setColor(ContextCompat.getColor(context, R.color.yellow4));
        m50PercentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        m50PercentPaint.setColor(ContextCompat.getColor(context, R.color.yellow3));
        m75PercentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        m75PercentPaint.setColor(ContextCompat.getColor(context, R.color.yellow2));
        m100PercentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        m100PercentPaint.setColor(ContextCompat.getColor(context, R.color.yellow1));
        mWhitePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mWhitePaint.setColor(ContextCompat.getColor(context, R.color.white));
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
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float percentage = (float)mMillisUntilFinished / (float) mTotalTime;
        canvas.drawRect(0, 0, mWidth, mHeight, m100PercentPaint);
        canvas.drawRect(0, 0, 0.75f * mWidth, mHeight, m75PercentPaint);
        canvas.drawRect(0, 0, 0.5f * mWidth, mHeight, m50PercentPaint);
        canvas.drawRect(0, 0, 0.25f * mWidth, mHeight, m25PercentPaint);
        canvas.drawRect(percentage * mWidth, 0, mWidth, mHeight, mWhitePaint);
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
        if (mIsPaused && mMillisUntilFinished > 0) {
            if (mTimer != null){
                mTimer.cancel();
                mTimer = null;
            }
            createNewTimer(mMillisUntilFinished);
            mTimer.start();
            mIsPaused = false;
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

    public interface Command {
        void execute();
    }
}

