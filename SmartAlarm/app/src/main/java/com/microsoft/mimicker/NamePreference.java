package com.microsoft.mimicker;

import android.content.Context;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;

public class NamePreference extends EditTextPreference {

    private boolean mChanged;

    public NamePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                String changedText = (String) o;
                if (getText() == null || getText().compareTo(changedText) != 0) {
                    setChanged(true);
                    setSummary(changedText);
                }
                return true;
            }
        });
    }

    public void setAlarmName(String alarmName) {
        setText(alarmName);
        setSummary(alarmName);
    }

    public boolean hasChanged() {
        return mChanged;
    }

    public void setChanged(boolean changed) {
        mChanged = changed;
    }

    private void setSummary(String alarmName) {
        if (alarmName == null || alarmName.isEmpty()) {
            String hint = getContext().getResources().getString(R.string.pref_title_description_hint);
            super.setSummary("[" + hint + "]");
        } else {
            super.setSummary(alarmName);
        }
    }
}
