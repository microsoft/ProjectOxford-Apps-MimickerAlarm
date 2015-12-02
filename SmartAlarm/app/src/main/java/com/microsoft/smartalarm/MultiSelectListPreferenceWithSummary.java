package com.microsoft.smartalarm;

import android.content.Context;
import android.support.v14.preference.MultiSelectListPreference;
import android.util.AttributeSet;

import java.util.Set;

public class MultiSelectListPreferenceWithSummary extends MultiSelectListPreference {

    public MultiSelectListPreferenceWithSummary(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void setSummaryValues(Set<String> values, int defaultResourceId) {
        CharSequence[] menuItems = getEntryValues();
        CharSequence[] menuItemsDisplay = getEntries();
        String summaryString = "";
        for (int i = 0; i < menuItems.length; i++) {
            if (values.contains(menuItems[i].toString())) {
                String displayString = menuItemsDisplay[i].toString();
                if (summaryString.isEmpty()) {
                    summaryString = displayString;
                } else {
                    summaryString += ", " + displayString;
                }
            }
        }
        if (summaryString.isEmpty()) {
            summaryString = getContext().getString(defaultResourceId);
        }
        setSummary(summaryString);
    }
}
