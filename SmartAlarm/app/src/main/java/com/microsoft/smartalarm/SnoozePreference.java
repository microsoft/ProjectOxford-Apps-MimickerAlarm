package com.microsoft.smartalarm;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.PreferenceViewHolder;
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;

import android.widget.CompoundButton;

public class SnoozePreference extends ListPreference {
    private SwitchCompat mSnoozeEnabled;

    public SnoozePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWidgetLayoutResource(R.layout.preference_snooze);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        mSnoozeEnabled = (SwitchCompat) holder.findViewById(R.id.snooze_switch);
        mSnoozeEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Snackbar.make(buttonView, "Snooze functionality coming soon!", Snackbar.LENGTH_SHORT).show();
            }
        });
    }
}
