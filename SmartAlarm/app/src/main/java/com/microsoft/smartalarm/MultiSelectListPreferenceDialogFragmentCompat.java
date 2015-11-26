package com.microsoft.smartalarm;


// reference from http://stackoverflow.com/questions/32621403/how-do-i-create-custom-preferences-using-android-support-v7-preference-library

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.support.v14.preference.MultiSelectListPreference;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.Preference;

import java.util.HashSet;
import java.util.Set;

public class MultiSelectListPreferenceDialogFragmentCompat extends PreferenceDialogFragmentCompat
        implements DialogPreference.TargetFragment {

    private boolean mDialogContentsChanged;
    private final Set<String> mLatestValues = new HashSet<>();

    public MultiSelectListPreferenceDialogFragmentCompat () {

    }

    public static MultiSelectListPreferenceDialogFragmentCompat newInstance(Preference preference) {
        MultiSelectListPreferenceDialogFragmentCompat fragment = new MultiSelectListPreferenceDialogFragmentCompat();
        Bundle bundle = new Bundle(1);
        bundle.putString("key", preference.getKey());
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public Preference findPreference(CharSequence charSequence) {
        return getPreference();
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);
        final MultiSelectListPreference preference = (MultiSelectListPreference) getPreference();
        if (isStateValid()) {
            builder.setMultiChoiceItems(preference.getEntries(), getCheckedItems(), new DialogInterface.OnMultiChoiceClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                    mDialogContentsChanged = true;
                    String changedValue = preference.getEntryValues()[which].toString();
                    if (isChecked) {
                        mLatestValues.add(changedValue);
                    } else {
                        mLatestValues.remove(changedValue);
                    }
                }
            });
            mDialogContentsChanged = false;
            mLatestValues.clear();
            mLatestValues.addAll(preference.getValues());
        }
    }

    @Override
    public void onDialogClosed(boolean resultIsOk) {
        if (resultIsOk && mDialogContentsChanged) {
            MultiSelectListPreference preference = (MultiSelectListPreference) getPreference();
            if (preference.callChangeListener(mLatestValues)) {
                preference.setValues(mLatestValues);
            }
        }
    }

    public boolean isStateValid () {
        MultiSelectListPreference preference = (MultiSelectListPreference) getPreference();
        return (preference.getEntries() != null)
                && (preference.getEntryValues() != null);
    }

    public boolean[] getCheckedItems() {
        MultiSelectListPreference preference = (MultiSelectListPreference) getPreference();
        CharSequence[] menuItems = preference.getEntryValues();
        Set<String> checkedMenuItems = preference.getValues();
        boolean[] checkedItems = new boolean[menuItems.length];
        for (int i = 0; i < menuItems.length; i++) {
            checkedItems[i] = checkedMenuItems.contains(menuItems[i].toString());
        }
        return checkedItems;
    }
}
