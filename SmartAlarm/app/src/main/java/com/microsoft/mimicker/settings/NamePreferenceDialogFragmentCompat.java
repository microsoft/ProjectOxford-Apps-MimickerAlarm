package com.microsoft.mimicker.settings;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.preference.EditTextPreferenceDialogFragmentCompat;
import android.support.v7.preference.Preference;
import android.text.InputFilter;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.microsoft.mimicker.R;

public class NamePreferenceDialogFragmentCompat extends EditTextPreferenceDialogFragmentCompat
        implements View.OnFocusChangeListener {

    private static final String ARGS_KEY = "key";
    private static int KEYBOARD_SHOW_DELAY = 100;
    private static int NAME_LENGTH_MAX = 50;
    private EditText mEditText;

    public static NamePreferenceDialogFragmentCompat newInstance(Preference preference) {
        NamePreferenceDialogFragmentCompat fragment = new NamePreferenceDialogFragmentCompat();
        Bundle bundle = new Bundle(1);
        bundle.putString(ARGS_KEY, preference.getKey());
        fragment.setArguments(bundle);
        return fragment;
    }


    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        mEditText = (EditText) view.findViewById(android.R.id.edit);
        mEditText.setSingleLine();
        mEditText.setSelection(mEditText.getText().length());
        mEditText.setOnFocusChangeListener(this);
        mEditText.setHint(R.string.pref_title_description_hint);
        mEditText.setFilters(new InputFilter[] {new InputFilter.LengthFilter(NAME_LENGTH_MAX)});
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            v.postDelayed(new Runnable() {
                @Override
                public void run() {
                    InputMethodManager keyboard = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    keyboard.showSoftInput(mEditText, InputMethodManager.SHOW_IMPLICIT);
                }
            }, KEYBOARD_SHOW_DELAY);
        }
    }
}
