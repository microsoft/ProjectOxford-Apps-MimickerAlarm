package com.microsoft.smartalarm;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.preference.EditTextPreferenceDialogFragmentCompat;
import android.support.v7.preference.Preference;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class NamePreferenceDialogFragmentCompat extends EditTextPreferenceDialogFragmentCompat
        implements View.OnFocusChangeListener {

    private EditText mEditText;
    private static int KEYBOARD_SHOW_DELAY = 500;

    private static final String ARGS_KEY = "key";

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
