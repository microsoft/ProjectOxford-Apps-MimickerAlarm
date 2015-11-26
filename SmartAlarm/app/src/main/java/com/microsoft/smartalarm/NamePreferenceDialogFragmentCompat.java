package com.microsoft.smartalarm;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.preference.EditTextPreferenceDialogFragmentCompat;
import android.support.v7.preference.Preference;
import android.view.View;
import android.widget.EditText;

public class NamePreferenceDialogFragmentCompat extends EditTextPreferenceDialogFragmentCompat {

    public static NamePreferenceDialogFragmentCompat newInstance(Preference preference) {
        NamePreferenceDialogFragmentCompat fragment = new NamePreferenceDialogFragmentCompat();
        Bundle bundle = new Bundle(1);
        bundle.putString("key", preference.getKey());
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        EditText editText = (EditText) view.findViewById(android.R.id.edit);
        editText.setSingleLine();
        editText.setSelection(editText.getText().length());
    }
}
