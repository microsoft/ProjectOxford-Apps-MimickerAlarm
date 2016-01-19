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

/**
 * A custom preference class that handles the settings for a repeating alarm.  This class takes
 * care of drawing the custom settings user experience for the different days of the week.
 */
public class RepeatingDaysPreference extends Preference {

    private boolean mChanged;
    private boolean[] mRepeatingDays;
    private String[] mDayNames;

    public RepeatingDaysPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mDayNames = DateTimeUtilities.getShortDayNames();
        mRepeatingDays = new boolean[7];
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        LinearLayout container = (LinearLayout) holder.findViewById(R.id.pref_repeating_container);
        container.removeAllViews();
        for(int i = 0; i < mRepeatingDays.length; i++) {
            DayView dayView = new DayView(getContext(), this, i);
            dayView.setText(mDayNames[i]);
            dayView.setRepeating(mRepeatingDays[i]);
            container.addView(dayView);
        }
        super.onBindViewHolder(holder);
    }

    public void setRepeatingDay(int index, boolean doesRepeat){
        mRepeatingDays[index] = doesRepeat;
    }

    public boolean hasChanged() {
        return mChanged;
    }

    public void setChanged(boolean changed) {
        mChanged = changed;
    }

    public boolean[] getRepeatingDays() {
        return mRepeatingDays;
    }

    private class DayView extends TextView {
        private Paint mPaint;
        private int mPadding;
        private RepeatingDaysPreference mParent;
        private int mDayIndex;

        public DayView(Context context, RepeatingDaysPreference parent, int dayIndex) {
            super(context);
            mParent = parent;
            mDayIndex = dayIndex;
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
            if (getRepeating()){
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
            return mParent.getRepeatingDays()[mDayIndex];
        }

        public void setRepeating(boolean repeating) {
            mParent.setRepeatingDay(mDayIndex, repeating);
        }
    }
}
