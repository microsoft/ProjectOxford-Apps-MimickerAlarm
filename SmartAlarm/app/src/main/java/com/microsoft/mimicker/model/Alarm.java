/*
 *
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license.
 *
 * Project Oxford: http://ProjectOxford.ai
 *
 * Project Oxford Mimicker Alarm Github:
 * https://github.com/Microsoft/ProjectOxford-Apps-MimickerAlarm
 *
 * Copyright (c) Microsoft Corporation
 * All rights reserved.
 *
 * MIT License:
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED ""AS IS"", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package com.microsoft.mimicker.model;

import android.content.Context;
import android.net.Uri;

import com.microsoft.mimicker.R;
import com.microsoft.mimicker.appcore.AlarmApplication;
import com.microsoft.mimicker.scheduling.AlarmScheduler;
import com.microsoft.mimicker.utilities.Logger;
import com.microsoft.mimicker.utilities.GeneralUtilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.UUID;

/**
 * This class is the representation of an Alarm within the app.  Apart from encapsulating all the
 * necessary data to help with alarm scheduling and the user experience, it exposes the following
 * alarm actions:
 *
 * schedule - updates the alarm state appropriately and schedules the alarm via the
 * AlarmScheduler, cancelling any previous alarms that may have been set.
 *
 * snooze - updates the alarm state and also sets the appropriate snooze data fields.  Schedules the
 * snoozed alarm via the AlarmScheduler.
 *
 * delete - cancels the alarm if necessary and removes it from the database.
 *
 * cancel - resets the enabled state and tells the AlarmScheduler to cancel the alarm.
 *
 * onDismiss - this method gets called when an alarm is dismissed in the user interface.  If the
 * alarm is oneshot it will be disabled, otherwise the next recurring alarm will be scheduled.
 */
public class Alarm {
    private static final int SNOOZE_DURATION_INTEGER = (5 * 60) * 1000;

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
    private int mSnoozeSeconds;

    public Alarm () {
        this(UUID.randomUUID());
    }

    public Alarm(UUID id) {
        mId = id;
        Calendar calendar = Calendar.getInstance();
        mTimeHour = calendar.get(Calendar.HOUR_OF_DAY);
        mTimeMinute = calendar.get(Calendar.MINUTE);
        mRepeatingDays = new boolean[]{ false, false, false, false, false, false, false };
        mAlarmTone = GeneralUtilities.defaultRingtone();
        mIsEnabled = true;
        mVibrate = true;
        mTongueTwisterEnabled = true;
        mColorCaptureEnabled = GeneralUtilities.deviceHasRearFacingCamera();
        mExpressYourselfEnabled = GeneralUtilities.deviceHasFrontFacingCamera();
        mNew = false;
        mSnoozed = false;
        mSnoozeHour = 0;
        mSnoozeMinute = 0;
        mSnoozeSeconds = 0;
        mTitle = AlarmApplication.getAppContext().getString(R.string.app_name);
    }

    public long schedule() {
        Context context = AlarmApplication.getAppContext();
        if (isEnabled() && !isNew()) {
            AlarmScheduler.cancelAlarm(context, this);
        } else {
            setIsEnabled(true);
        }
        // If someone edits alarm settings while in a snooze period we reset the snooze
        setSnoozed(false);
        setNew(false);
        AlarmList.get(context).updateAlarm(this);
        return AlarmScheduler.scheduleAlarm(context, this);
    }

    public void snooze() {
        Context context = AlarmApplication.getAppContext();
        // Schedule the snooze and update the alarm data with the details
        long snoozeTime = AlarmScheduler.snoozeAlarm(context, this, getAlarmSnoozeDuration());
        Calendar snoozeCalendar = Calendar.getInstance();
        snoozeCalendar.setTimeInMillis(snoozeTime);
        setSnoozeHour(snoozeCalendar.get(Calendar.HOUR_OF_DAY));
        setSnoozeMinute(snoozeCalendar.get(Calendar.MINUTE));
        setSnoozeSeconds(snoozeCalendar.get(Calendar.SECOND));
        setSnoozed(true);
        setIsEnabled(true);
        AlarmList.get(context).updateAlarm(this);
    }

    public void delete() {
        Context context = AlarmApplication.getAppContext();
        if (isEnabled()) {
            AlarmScheduler.cancelAlarm(context, this);
        }
        AlarmList.get(context).deleteAlarm(this);
    }

    public void cancel() {
        Context context = AlarmApplication.getAppContext();
        setIsEnabled(false);
        // Reset the snooze state if we are cancelling the alarm
        setSnoozed(false);
        AlarmScheduler.cancelAlarm(context, this);
        AlarmList.get(context).updateAlarm(this);
    }

    public void onDismiss() {
        Context context = AlarmApplication.getAppContext();
        boolean updateAlarm = false;
        if (isOneShot()) {
            // We disable a oneshot alarm after it has been dismissed
            setIsEnabled(false);
            updateAlarm = true;

        } else {
            // Schedule the next repeating alarm if necessary
            AlarmScheduler.scheduleAlarm(context, this);
        }

        if (isSnoozed()) {
            setSnoozed(false);
            updateAlarm = true;
        }

        if (updateAlarm) {
            AlarmList.get(context).updateAlarm(this);
        }
    }

    private int getAlarmSnoozeDuration() {
        return GeneralUtilities.getDurationSetting(R.string.pref_snooze_duration_key,
                R.string.pref_default_snooze_duration_value,
                SNOOZE_DURATION_INTEGER);
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
        mSnoozeMinute = snoozeMinute;
    }

    public int getSnoozeSeconds() {
        return mSnoozeSeconds;
    }

    public void setSnoozeSeconds(int snoozeSeconds) {
        mSnoozeSeconds = snoozeSeconds;
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
