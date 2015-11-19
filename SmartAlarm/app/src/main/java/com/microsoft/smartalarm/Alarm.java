package com.microsoft.smartalarm;

import android.net.Uri;

import java.util.UUID;

public class Alarm {
    private UUID    mId;
    private String  mTitle;
    private int     mTimeHour;
    private int     mTimeMinute;
    private boolean mRepeatingDays[];
    private boolean mRepeatWeekly;
    private Uri     mAlarmTone;
    private boolean mIsEnabled;

    public Alarm () {
        this(UUID.randomUUID());
    }

    public Alarm(UUID id) {
        mId = id;
        mTimeHour = 12;
        mTimeMinute = 0;
        mRepeatWeekly = true;
        mRepeatingDays = new boolean[]{ true, true, true, true, true, true, true };
        mAlarmTone = Uri.parse("content://settings/system/ringtone");
        mIsEnabled = true;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public boolean isEnabled() {
        return mIsEnabled;
    }

    public void setIsEnabled(boolean isEnabled) {
        mIsEnabled = isEnabled;
    }

    public UUID getId() {
        return mId;
    }

    public int getTimeHour() {
        return mTimeHour;
    }

    public void setTimeHour(int timeHour) {
        mTimeHour = timeHour;
    }

    public int getTimeMinute() {
        return mTimeMinute;
    }

    public void setTimeMinute(int timeMinute) {
        mTimeMinute = timeMinute;
    }

    public boolean isRepeatWeekly() {

        return mRepeatWeekly;
    }

    public void setRepeatWeekly(boolean repeatWeekly) {
        mRepeatWeekly = repeatWeekly;
    }

    public void setRepeatingDay(int dayOfWeek, boolean value) {
        mRepeatingDays[dayOfWeek] = value;
    }

    public Uri getAlarmTone() {
        return mAlarmTone;
    }

    public void setAlarmTone(Uri alarmTone) {
        mAlarmTone = alarmTone;
    }

    public boolean getRepeatingDay(int dayOfWeek) {
        return mRepeatingDays[dayOfWeek];
    }
}
