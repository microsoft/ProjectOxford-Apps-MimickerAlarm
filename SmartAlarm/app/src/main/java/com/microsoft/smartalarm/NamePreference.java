package com.microsoft.smartalarm;

import android.content.Context;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;

public class NamePreference extends EditTextPreference {

    public NamePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                setSummary((String)o);
                return true;
            }
        });
    }

    public void setAlarmName(String alarmName) {
        setText(alarmName);
        setSummary(alarmName);
    }
}
