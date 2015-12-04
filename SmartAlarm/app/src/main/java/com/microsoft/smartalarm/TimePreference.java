package com.microsoft.smartalarm;

import android.content.Context;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.Format;
import java.util.Calendar;

public class TimePreference extends DialogPreference {

    private TextView mTimeLabel;
    private int mHour;
    private int mMinute;
    private boolean mChanged;

    public TimePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.preference_time);
        setDialogLayoutResource(R.layout.preference_timedialog);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        mTimeLabel = (TextView) holder.findViewById(R.id.time_label);
        setTimeLabel();
    }

    public void setTime(int hour, int minute) {
        mHour = hour;
        mMinute = minute;
    }

    public int getMinute() {
        return mMinute;
    }

    public int getHour() {
        return mHour;
    }

    private void setTimeLabel() {
        Format formatter = DateFormat.getTimeInstance(DateFormat.SHORT);
        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.HOUR_OF_DAY, mHour);
        calendar.set(Calendar.MINUTE, mMinute);

        mTimeLabel.setText(formatter.format(calendar.getTime()));
    }

    public boolean hasChanged() {
        return mChanged;
    }

    public void setChanged(boolean changed) {
        mChanged = changed;
        setTimeLabel();
    }
}
