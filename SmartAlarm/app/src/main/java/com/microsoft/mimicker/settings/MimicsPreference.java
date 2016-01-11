package com.microsoft.mimicker.settings;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;

import com.microsoft.mimicker.R;

import java.util.HashSet;
import java.util.Set;

public class MimicsPreference extends Preference {

    private boolean mTongueTwisterEnabled;
    private boolean mColorCaptureEnabled;
    private boolean mExpressYourselfEnabled;
    private String[] mMimicLabels;
    private String[] mMimicValues;
    private Set<String> mInitialValues;
    private Set<String> mEnabledValues;

    public MimicsPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public boolean hasChanged() {
        return mInitialValues.equals(mEnabledValues);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
    }

    public boolean isTongueTwisterEnabled() {
        return mTongueTwisterEnabled;
    }

    public void setTongueTwisterEnabled(boolean tongueTwisterEnabled) {
        mTongueTwisterEnabled = tongueTwisterEnabled;
    }

    public boolean isColorCaptureEnabled() {
        return mColorCaptureEnabled;
    }

    public void setColorCaptureEnabled(boolean colorCaptureEnabled) {
        mColorCaptureEnabled = colorCaptureEnabled;
    }

    public boolean isExpressYourselfEnabled() {
        return mExpressYourselfEnabled;
    }

    public void setExpressYourselfEnabled(boolean expressYourselfEnabled) {
        mExpressYourselfEnabled = expressYourselfEnabled;
    }

    public void setMimicValuesAndSummary(String[] enabledMimics) {
        mEnabledValues.clear();

        for (String mimic : enabledMimics) {
            mEnabledValues.add(mimic);
        }

        setTongueTwisterEnabled(mEnabledValues.contains(getContext().getString(R.string.pref_mimic_tongue_twister_id)));
        setColorCaptureEnabled(mEnabledValues.contains(getContext().getString(R.string.pref_mimic_color_capture_id)));
        setExpressYourselfEnabled(mEnabledValues.contains(getContext().getString(R.string.pref_mimic_express_yourself_id)));

        setSummaryValues(mEnabledValues);
    }

    public void setInitialValues() {
        mMimicValues = getContext().getResources().getStringArray(R.array.pref_mimic_values);
        mMimicLabels = getContext().getResources().getStringArray(R.array.pref_mimic_labels);
        mInitialValues = new HashSet<>();
        mEnabledValues = new HashSet<>();
        if (isTongueTwisterEnabled()) {
            mInitialValues.add(getContext().getString(R.string.pref_mimic_tongue_twister_id));
            mEnabledValues.add(getContext().getString(R.string.pref_mimic_tongue_twister_id));
        }
        if (isColorCaptureEnabled()) {
            mInitialValues.add(getContext().getString(R.string.pref_mimic_color_capture_id));
            mEnabledValues.add(getContext().getString(R.string.pref_mimic_tongue_twister_id));
        }
        if (isExpressYourselfEnabled()) {
            mInitialValues.add(getContext().getString(R.string.pref_mimic_express_yourself_id));
            mEnabledValues.add(getContext().getString(R.string.pref_mimic_tongue_twister_id));
        }
    }

    public void setInitialSummary() {
        setSummaryValues(mInitialValues);
    }

    public String[] getEnabledMimicValues() {
        return mEnabledValues.toArray(new String[mEnabledValues.size()]);
    }

    private void setSummaryValues(Set<String> values) {
        String summaryString = "";
        for (int i = 0; i < mMimicValues.length; i++) {
            if (values.contains(mMimicValues[i])) {
                String displayString = mMimicLabels[i];
                if (summaryString.isEmpty()) {
                    summaryString = displayString;
                } else {
                    summaryString += ", " + displayString;
                }
            }
        }
        if (summaryString.isEmpty()) {
            summaryString = getContext().getString(R.string.pref_no_mimics);
        }
        setSummary(summaryString);
    }
}
