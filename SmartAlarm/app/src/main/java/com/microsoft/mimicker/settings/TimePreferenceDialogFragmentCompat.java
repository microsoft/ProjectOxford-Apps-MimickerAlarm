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
