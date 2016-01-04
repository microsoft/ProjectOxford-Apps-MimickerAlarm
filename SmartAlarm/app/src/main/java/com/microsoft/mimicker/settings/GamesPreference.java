package com.microsoft.mimicker.settings;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;

import com.microsoft.mimicker.R;

import java.util.HashSet;
import java.util.Set;

public class GamesPreference extends MultiSelectListPreferenceWithSummary {

    private boolean mTongueTwisterEnabled;
    private boolean mColorCollectorEnabled;
    private boolean mExpressYourselfEnabled;
    private boolean mChanged;

    public GamesPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public boolean hasChanged() {
        return mChanged;
    }

    public void setChanged(boolean changed) {
        mChanged = changed;
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
                setChanged(true);
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
            values.add(getContext().getString(R.string.pref_game_tongue_twister_id));
        }
        if (isColorCollectorEnabled()) {
            values.add(getContext().getString(R.string.pref_game_color_collector_id));
        }
        if (isExpressYourselfEnabled()) {
            values.add(getContext().getString(R.string.pref_game_express_yourself_id));
        }
        setValues(values);
        setSummaryValues(values, R.string.pref_no_game);
    }

    private void setGamePreferences(Set<String> values) {
        setTongueTwisterEnabled(values.contains(getContext().getString(R.string.pref_game_tongue_twister_id)));
        setColorCollectorEnabled(values.contains(getContext().getString(R.string.pref_game_color_collector_id)));
        setExpressYourselfEnabled(values.contains(getContext().getString(R.string.pref_game_express_yourself_id)));
        setSummaryValues(values, R.string.pref_no_game);
    }
}
