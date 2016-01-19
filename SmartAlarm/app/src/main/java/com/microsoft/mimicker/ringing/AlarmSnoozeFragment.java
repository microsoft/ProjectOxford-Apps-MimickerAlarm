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

package com.microsoft.mimicker.ringing;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.microsoft.mimicker.R;

public class AlarmSnoozeFragment extends Fragment {
    public static final String SNOOZE_FRAGMENT_TAG = "snooze_fragment";
    private static final int SNOOZE_SCREEN_TIMEOUT_DURATION = 3 * 1000;
    SnoozeResultListener mCallback;
    private Handler mHandler;
    private Runnable mAutoDismissTask;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_snooze, container, false);
        TextView snoozeDuration = (TextView) view.findViewById(R.id.alarm_snoozed_duration);
        snoozeDuration.setText(getAlarmSnoozeDuration());
        mAutoDismissTask = new Runnable() {
            @Override
            public void run() {
                mCallback.onSnoozeDismiss();
            }
        };
        mHandler = new Handler();
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallback = (SnoozeResultListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    @Override
    public void onPause() {
        mHandler.removeCallbacks(mAutoDismissTask);
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mHandler.postDelayed(mAutoDismissTask, SNOOZE_SCREEN_TIMEOUT_DURATION);

    }

    private String getAlarmSnoozeDuration() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        return preferences.getString(getString(R.string.pref_snooze_duration_display_key),
                getString(R.string.pref_default_snooze_duration_label));
    }

    public interface SnoozeResultListener {
        void onSnoozeDismiss();
    }
}
