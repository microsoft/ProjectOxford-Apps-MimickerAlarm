package com.microsoft.smartalarm;

import android.media.RingtoneManager;
import android.net.Uri;

import java.util.Calendar;
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
    private boolean mVibrate;
    private boolean mTongueTwisterEnabled;
    private boolean mColorCollectorEnabled;
    private boolean mExpressYourselfEnabled;

    public Alarm () {
        this(UUID.randomUUID());
    }

    public Alarm(UUID id) {
        mId = id;
        Calendar calendar = Calendar.getInstance();
        mTimeHour = calendar.getTime().getHours();
        mTimeMinute = calendar.getTime().getMinutes();
        mRepeatWeekly = true;
        mRepeatingDays = new boolean[]{ true, true, true, true, true, true, true };
        mAlarmTone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALL);
        mIsEnabled = true;
        mVibrate = true;
        mTongueTwisterEnabled = true;
        mColorCollectorEnabled = true;
        mExpressYourselfEnabled = true;
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

    public boolean shouldVibrate() {
        return mVibrate;
    }

    public void setVibrate(boolean vibrate) {
        mVibrate = vibrate;
    }

    public boolean isExpressYourselfEnabled() {
        return mExpressYourselfEnabled;
    }

    public void setExpressYourselfEnabled(boolean expressYourselfEnabled) {
        mExpressYourselfEnabled = expressYourselfEnabled;
    }

    public boolean isColorCollectorEnabled() {
        return mColorCollectorEnabled;
    }

    public void setColorCollectorEnabled(boolean colorCollectorEnabled) {
        mColorCollectorEnabled = colorCollectorEnabled;
    }

    public boolean isTongueTwisterEnabled() {
        return mTongueTwisterEnabled;
    }

    public void setTongueTwisterEnabled(boolean tongueTwister) {
        mTongueTwisterEnabled = tongueTwister;
    }
}
