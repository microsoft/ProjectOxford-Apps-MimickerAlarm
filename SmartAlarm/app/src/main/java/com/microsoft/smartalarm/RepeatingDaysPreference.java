package com.microsoft.smartalarm;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;

public class RepeatingDaysPreference extends Preference {

    private boolean mLayoutInitialized;
    private boolean mChanged;
    private DayView[] mDayViews;

    public RepeatingDaysPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mDayViews = new DayView[Calendar.SATURDAY];
        for(int d = Calendar.SUNDAY, i = 0; d <= Calendar.SATURDAY; d++, i++){
            DayView dayView = new DayView(getContext(), this);
            dayView.setText(DateUtils.getDayOfWeekString(d, DateUtils.LENGTH_SHORT).toUpperCase());
            mDayViews[i] = dayView;
        }
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        if (!mLayoutInitialized) {
            LinearLayout container = (LinearLayout) holder.findViewById(R.id.pref_repeating_container);
            for(DayView day : mDayViews){
                day.setLayoutParams(new LinearLayout.LayoutParams(0,
                        LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
                container.addView(day);
            }
            mLayoutInitialized = true;
        }
        super.onBindViewHolder(holder);
    }

    public void setRepeatingDay(int index, boolean doesRepeat){
        mDayViews[index].setRepeating(doesRepeat);
    }

    public boolean hasChanged() {
        return mChanged;
    }

    public void setChanged(boolean changed) {
        mChanged = changed;
    }

    public boolean[] getRepeatingDays() {
        boolean[] repeatingDays = new boolean[mDayViews.length];
        for(int i = 0; i < mDayViews.length; i++){
            repeatingDays[i] = mDayViews[i].getRepeating();
        }
        return repeatingDays;
    }


    private class DayView extends TextView {
        private boolean mRepeating = false;
        private Paint mPaint;
        private final static int PADDING = 20;
        private RepeatingDaysPreference mParent;

        public DayView(Context context, RepeatingDaysPreference parent) {
            super(context);
            mParent = parent;
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setColor(Color.RED);

            setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleRepeating();
                }
            });
            setGravity(Gravity.CENTER);
        }

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            super.onLayout(changed, left, top, right, bottom);
            setHeight(getMeasuredWidth());
            setPadding(PADDING, PADDING, PADDING, PADDING);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            if (mRepeating){
                /*
                float centerX = getWidth() / 2;
                float centerY = getHeight() / 2;
                canvas.drawCircle(centerX, centerY, centerX - PADDING, mPaint);
                */
                setTypeface(null, Typeface.BOLD);
            }
            else{
                setTypeface(null, Typeface.NORMAL);
            }
            super.onDraw(canvas);
        }

        public void setRepeating(boolean repeating){
            mRepeating = repeating;
            invalidate();
        }

        public void toggleRepeating(){
            setRepeating(!getRepeating());
            mParent.setChanged(true);
        }

        public boolean getRepeating() {
            return mRepeating;
        }
    }
}
