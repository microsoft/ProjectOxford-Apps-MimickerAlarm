package com.microsoft.mimicker.settings;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.util.AttributeSet;

import com.microsoft.mimicker.R;
import com.microsoft.mimicker.model.Alarm;

import java.util.ArrayList;

/**
 * A custom preferences class that encapsulates the Mimics settings for an alarm.
 */
public class MimicsPreference extends Preference {
    private String[] mMimicLabels;
    private String[] mMimicValues;
    ArrayList<String> mInitialValues;
    ArrayList<String> mEnabledValues;

    public static ArrayList<String> getEnabledMimics(Context context, Alarm alarm) {
        ArrayList<String> enabledMimics = new ArrayList<>();
        if (alarm.isColorCaptureEnabled()) {
            enabledMimics.add(context.getString(R.string.pref_mimic_color_capture_id));
        }
        if (alarm.isExpressYourselfEnabled()) {
            enabledMimics.add(context.getString(R.string.pref_mimic_express_yourself_id));
        }
        if (alarm.isTongueTwisterEnabled()) {
            enabledMimics.add(context.getString(R.string.pref_mimic_tongue_twister_id));
        }
        return enabledMimics;
    }

    public MimicsPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public boolean hasChanged() {
        return !mInitialValues.equals(mEnabledValues);
    }

    public boolean isTongueTwisterEnabled() {
        return mEnabledValues.contains(getContext().getString(R.string.pref_mimic_tongue_twister_id));
    }

    public boolean isColorCaptureEnabled() {
        return mEnabledValues.contains(getContext().getString(R.string.pref_mimic_color_capture_id));
    }

    public boolean isExpressYourselfEnabled() {
        return mEnabledValues.contains(getContext().getString(R.string.pref_mimic_express_yourself_id));
    }

    public void setMimicValuesAndSummary(ArrayList<String> enabledMimics) {
        mEnabledValues = enabledMimics;
        setSummaryValues(mEnabledValues);
    }

    public void setInitialValues(Alarm alarm) {
        mMimicValues = getContext().getResources().getStringArray(R.array.pref_mimic_values);
        mMimicLabels = getContext().getResources().getStringArray(R.array.pref_mimic_labels);
        mEnabledValues = getEnabledMimics(getContext(), alarm);

        // Save the initial state so we can check for changes later
        mInitialValues = new ArrayList<>(mEnabledValues);
    }

    public void setInitialSummary() {
        setSummaryValues(mInitialValues);
    }

    public ArrayList<String> getEnabledMimicValues() {
        return mEnabledValues;
    }

    private void setSummaryValues(ArrayList<String> values) {
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
