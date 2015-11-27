package com.microsoft.smartalarm;

import android.content.Context;
import android.support.v14.preference.MultiSelectListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;

import java.util.Set;


public class RepeatingDaysPreference extends MultiSelectListPreference {

    private boolean mDirty;
    private boolean[] mRepeatingDays;

    public RepeatingDaysPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mRepeatingDays = new boolean[getEntryValues().length];
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                @SuppressWarnings("unchecked")
                Set<String> repeatingDays = (Set<String>) o;
                setRepeatingDays(repeatingDays);
                setDirty(true);
                return false;
            }
        });
    }

    public boolean isDirty() {
        return mDirty;
    }

    public void setDirty(boolean dirty) {
        mDirty = dirty;
    }

    private void setRepeatingDays(Set<String> values) {
        CharSequence[] menuItems = getEntryValues();
        for (int i = 0; i < menuItems.length; i++) {
            mRepeatingDays[i] = values.contains(menuItems[i].toString());
        }
    }

    public boolean[] getRepeatingDays() {
        return mRepeatingDays;
    }
}
