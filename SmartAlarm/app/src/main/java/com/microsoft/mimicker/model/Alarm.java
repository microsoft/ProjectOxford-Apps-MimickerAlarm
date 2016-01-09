package com.microsoft.mimicker.model;

import android.net.Uri;

import com.microsoft.mimicker.utilities.Logger;
import com.microsoft.mimicker.utilities.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.UUID;

public class Alarm {
    private UUID    mId;
    private String  mTitle;
    private int     mTimeHour;
    private int     mTimeMinute;
    private boolean mRepeatingDays[];
    private Uri     mAlarmTone;
    private boolean mIsEnabled;
    private boolean mVibrate;
    private boolean mTongueTwisterEnabled;
    private boolean mColorCaptureEnabled;
    private boolean mExpressYourselfEnabled;
    private boolean mNew;
    private boolean mSnoozed;
    private int mSnoozeHour;
    private int mSnoozeMinute;

    public Alarm () {
        this(UUID.randomUUID());
    }

    public Alarm(UUID id) {
        mId = id;
        Calendar calendar = Calendar.getInstance();
        mTimeHour = calendar.getTime().getHours();
        mTimeMinute = calendar.getTime().getMinutes();
        mRepeatingDays = new boolean[]{ false, false, false, false, false, false, false };
        mAlarmTone = Util.defaultRingtone();
        mIsEnabled = true;
        mVibrate = true;
        mTongueTwisterEnabled = true;
        mColorCaptureEnabled = true;
        mExpressYourselfEnabled = true;
        mNew = false;
        mSnoozed = false;
        mSnoozeHour = 0;
        mSnoozeMinute = 0;
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

    public boolean isColorCaptureEnabled() {
        return mColorCaptureEnabled;
    }

    public void setColorCaptureEnabled(boolean colorCaptureEnabled) {
        mColorCaptureEnabled = colorCaptureEnabled;
    }

    public boolean isTongueTwisterEnabled() {
        return mTongueTwisterEnabled;
    }

    public void setTongueTwisterEnabled(boolean tongueTwister) {
        mTongueTwisterEnabled = tongueTwister;
    }

    public boolean isNew() {
        return mNew;
    }

    public void setNew(boolean isNew) {
        mNew = isNew;
    }

    public boolean isSnoozed() {
        return mSnoozed;
    }

    public void setSnoozed(boolean snoozed) {
        mSnoozed = snoozed;
    }

    public int getSnoozeHour() {
        return mSnoozeHour;
    }

    public void setSnoozeHour(int snoozeHour) {
        mSnoozeHour = snoozeHour;
    }

    public int getSnoozeMinute() {
        return mSnoozeMinute;
    }

    public void setSnoozeMinute(int snoozeMinute) {
        this.mSnoozeMinute = snoozeMinute;
    }

    public boolean isOneShot() {
        boolean isOneShot = true;
        for (int dayOfWeek = Calendar.SUNDAY; dayOfWeek <= Calendar.SATURDAY; ++dayOfWeek) {
            if (getRepeatingDay(dayOfWeek - 1)) {
                isOneShot = false;
                break;
            }
        }
        return isOneShot;
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        try {
            json.put("Alarm Id", getId());
            json.put("Alarm Vibrate", shouldVibrate());
            json.put("Alarm Time Hour", getTimeHour());
            json.put("Alarm Time Minute", getTimeMinute());

            JSONArray repeating = new JSONArray();
            for (boolean mRepeatingDay : mRepeatingDays) {
                repeating.put(mRepeatingDay);
            }
            json.put("Alarm Repeat", repeating);
        }
        catch (JSONException ex) {
            Logger.trackException(ex);
        }
        return json;
    }
}
