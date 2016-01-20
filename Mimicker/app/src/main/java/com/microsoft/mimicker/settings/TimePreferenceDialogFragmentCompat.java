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

package com.microsoft.mimicker.settings;

import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.TimePicker;

import com.microsoft.mimicker.R;

/**
 * This class is a specialization of a PreferenceDialogFragment so that we can host the time picker
 * of choice, depending on the host platform.
 *
 * If the platform is Lollipop or newer we show the new clock picker, otherwise we show the spinner
 * time picker. We query the system settings to ensure the picker is set to 24 hour format or not.
 */
public class TimePreferenceDialogFragmentCompat extends PreferenceDialogFragmentCompat {

    private static final String ARGS_KEY = "key";
    TimePicker mTimePicker;

    public TimePreferenceDialogFragmentCompat() {

    }

    public static TimePreferenceDialogFragmentCompat newInstance(Preference preference) {
        TimePreferenceDialogFragmentCompat fragment = new TimePreferenceDialogFragmentCompat();
        Bundle bundle = new Bundle(1);
        bundle.putString(ARGS_KEY, preference.getKey());
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        TimePreference timePreference = (TimePreference) getPreference();
        mTimePicker = (TimePicker) view.findViewById(R.id.pref_time_picker);
        mTimePicker.setIs24HourView(DateFormat.is24HourFormat(getContext()));
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            mTimePicker.setHour(timePreference.getHour());
            mTimePicker.setMinute(timePreference.getMinute());
        } else {
            mTimePicker.setCurrentHour(timePreference.getHour());
            mTimePicker.setCurrentMinute(timePreference.getMinute());
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onDialogClosed(boolean resultIsOk) {
        if (resultIsOk) {
            int hour, minute;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                hour = mTimePicker.getHour();
                minute = mTimePicker.getMinute();
            } else {
                hour = mTimePicker.getCurrentHour();
                minute = mTimePicker.getCurrentMinute();
            }

            TimePreference preference = (TimePreference) getPreference();
            preference.setTime(hour, minute);
            preference.setChanged(true);
        }
    }
}
