package com.microsoft.mimicker.settings;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.microsoft.mimicker.R;
import com.microsoft.mimicker.utilities.DateTimeUtilities;

public class RepeatingDaysPreference extends Preference {

    private boolean mLayoutInitialized;
    private boolean mChanged;
    private DayView[] mDayViews;

    public RepeatingDaysPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mDayViews = new DayView[7];

        //TODO How do we handle this better from a locale perspective i.e first day of week
        String[] dayNames = DateTimeUtilities.getShortDayNames();
        for(int i = 0; i < 7; i++) {
            DayView dayView = new DayView(getContext(), this);
            dayView.setText(dayNames[i]);
            mDayViews[i] = dayView;
        }
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        if (!mLayoutInitialized) {
            LinearLayout container = (LinearLayout) holder.findViewById(R.id.pref_repeating_container);
            for(DayView day : mDayViews){
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
        private int mPadding;
        private RepeatingDaysPreference mParent;

        public DayView(Context context, RepeatingDaysPreference parent) {
            super(context);
            mParent = parent;
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setColor(ContextCompat.getColor(context, R.color.yellow2));

            mPadding = context.getResources().getDimensionPixelSize(R.dimen.repeating_day_padding);
            setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            setHeight(context.getResources().getDimensionPixelSize(R.dimen.repeating_day_height));
            setPadding(mPadding, mPadding, mPadding, mPadding);
            setGravity(Gravity.CENTER);

            setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleRepeating();
                }
            });
        }

        @Override
        protected void onDraw(Canvas canvas) {
            if (mRepeating){
                float centerX = getWidth() / 2;
                float centerY = getHeight() / 2;
                canvas.drawCircle(centerX, centerY, centerY - mPadding, mPaint);
                setTypeface(null, Typeface.BOLD);
            }
            else{
                setTypeface(null, Typeface.NORMAL);
            }
            super.onDraw(canvas);
        }

        public void toggleRepeating(){
            setRepeating(!getRepeating());
            mParent.setChanged(true);
            invalidate();
        }

        public boolean getRepeating() {
            return mRepeating;
        }

        public void setRepeating(boolean repeating) {
            mRepeating = repeating;
        }
    }
}
