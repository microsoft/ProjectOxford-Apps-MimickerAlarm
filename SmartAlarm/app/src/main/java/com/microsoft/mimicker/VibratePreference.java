package com.microsoft.mimicker;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.support.v7.preference.SwitchPreferenceCompat;
import android.util.AttributeSet;

public class VibratePreference extends SwitchPreferenceCompat {

    public boolean mChanged;
    public boolean mInitiallyChecked;

    public VibratePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public boolean hasChanged() {
        return mChanged;
    }

    public void setChanged(boolean changed) {
        mChanged = changed;
    }

    public void setInitialValue(boolean checked) {
        setChecked(checked);
        mInitiallyChecked = checked;
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                setChanged(mInitiallyChecked != (boolean) o);
                return true;
            }
        });
    }
}
