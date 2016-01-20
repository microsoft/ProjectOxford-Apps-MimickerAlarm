/*
 *
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license.
 *
 * Project Oxford: http://ProjectOxford.ai
 *
 * Project Oxford Mimicker Alarm Github:
 * https://github.com/Microsoft/ProjectOxford-Apps-MimickerAlarm
 *
 * Copyright (c) Microsoft Corporation
 * All rights reserved.
 *
 * MIT License:
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED ""AS IS"", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

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

/**
 * This is a custom DialogFragment that allows us to customize the EditTextPreferenceDialog.  We
 * create a custom dialog to enable the following features:
 *
 *  Auto-invoke of the soft keyboard on showing of the dialog
 *  Overriding of the text input field parameters
 */
public class NamePreferenceDialogFragmentCompat extends EditTextPreferenceDialogFragmentCompat
        implements View.OnFocusChangeListener {

    private static final String ARGS_KEY = "key";
    private static int KEYBOARD_SHOW_DELAY = 100;
    private static int NAME_LENGTH_MAX = 33;
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
