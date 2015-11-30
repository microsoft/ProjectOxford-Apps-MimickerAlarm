package com.microsoft.smartalarm;

import android.content.Context;
import android.support.v14.preference.MultiSelectListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;

import java.util.HashSet;
import java.util.Set;

public class GamesPreference extends MultiSelectListPreference {

    private boolean mTongueTwisterEnabled;
    private boolean mColorCollectorEnabled;
    private boolean mExpressYourselfEnabled;
    private boolean mDirty;

    public GamesPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public boolean isDirty() {
        return mDirty;
    }

    public void setDirty(boolean dirty) {
        mDirty = dirty;
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                @SuppressWarnings("unchecked")
                Set<String> selectedGames = (Set<String>) o;
                setGamePreferences(selectedGames);
                setDirty(true);
                return true;
            }
        });
    }

    public boolean isTongueTwisterEnabled() {
        return mTongueTwisterEnabled;
    }

    public void setTongueTwisterEnabled(boolean tongueTwisterEnabled) {
        mTongueTwisterEnabled = tongueTwisterEnabled;
    }

    public boolean isColorCollectorEnabled() {
        return mColorCollectorEnabled;
    }

    public void setColorCollectorEnabled(boolean colorCollectorEnabled) {
        mColorCollectorEnabled = colorCollectorEnabled;
    }

    public boolean isExpressYourselfEnabled() {
        return mExpressYourselfEnabled;
    }

    public void setExpressYourselfEnabled(boolean expressYourselfEnabled) {
        mExpressYourselfEnabled = expressYourselfEnabled;
    }

    public void setInitialValues() {
        Set<String> values = new HashSet<>();
        if (isTongueTwisterEnabled()) {
            values.add("tongue_twister");
        }
        if (isColorCollectorEnabled()) {
            values.add("color_collector");
        }
        if (isExpressYourselfEnabled()) {
            values.add("express_yourself");
        }
        setValues(values);
        setSummaryValues(values);
    }

    private void setGamePreferences(Set<String> values) {
        setTongueTwisterEnabled(values.contains("tongue_twister"));
        setColorCollectorEnabled(values.contains("color_collector"));
        setExpressYourselfEnabled(values.contains("express_yourself"));
        setSummaryValues(values);
    }

    public void setSummaryValues(Set<String> values) {
        CharSequence[] menuItems = getEntryValues();
        CharSequence[] menuItemsDisplay = getEntries();
        String summaryString = "";
        for (int i = 0; i < menuItems.length; i++) {
            if (values.contains(menuItems[i].toString())) {
                String gameName = menuItemsDisplay[i].toString();
                if (summaryString.isEmpty()) {
                    summaryString = gameName;
                } else {
                    summaryString += ", " + gameName;
                }
            }
        }
        if (summaryString.isEmpty()) {
            summaryString = getContext().getString(R.string.pref_no_game);
        }
        setSummary(summaryString);
    }
}
