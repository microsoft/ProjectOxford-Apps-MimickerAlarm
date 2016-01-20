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
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.microsoft.mimicker.R;
import com.microsoft.mimicker.model.Alarm;
import com.microsoft.mimicker.model.AlarmList;

import java.util.UUID;

/**
 * This class handles the user experience when a user dismisses an alarm that has no Mimics
 * selected.  This screen gives the user the option to open the Mimics setting page of the
 * dismissed alarm so that they can set and play a Mimic the next time the alarm rings.  This screen
 * will timeout if the user does not interact with it.
 */
public class AlarmNoMimicsFragment extends Fragment {
    public static final String NO_MIMICS_FRAGMENT_TAG = "no_mimics_fragment";
    private static final String ARGS_ALARM_ID = "alarm_id";
    private static final int NOGAME_SCREEN_TIMEOUT_DURATION = 5 * 1000;
    NoMimicResultListener mCallback;
    private Handler mHandler;
    private Runnable mAutoDismissTask;

    public static AlarmNoMimicsFragment newInstance(String alarmId) {
        AlarmNoMimicsFragment fragment = new AlarmNoMimicsFragment();
        Bundle bundle = new Bundle(1);
        bundle.putString(ARGS_ALARM_ID, alarmId);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_no_mimics, container, false);

        Bundle args = getArguments();
        UUID alarmId = UUID.fromString(args.getString(ARGS_ALARM_ID));
        Alarm alarm = AlarmList.get(getContext()).getAlarm(alarmId);

        String name = alarm.getTitle();
        if (name != null && !name.isEmpty()) {
            TextView alarmTitle = (TextView) view.findViewById(R.id.alarm_no_mimics_label);
            alarmTitle.setText(name);
            alarmTitle.setVisibility(View.VISIBLE);
        }

        view.findViewById(R.id.alarm_no_mimics_tap_to_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandler.removeCallbacks(mAutoDismissTask);
                mCallback.onNoMimicDismiss(true);
            }
        });

        mAutoDismissTask = new Runnable() {
            @Override
            public void run() {
                mCallback.onNoMimicDismiss(false);
            }
        };
        mHandler = new Handler();
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallback = (NoMimicResultListener) context;
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
        mHandler.postDelayed(mAutoDismissTask, NOGAME_SCREEN_TIMEOUT_DURATION);
    }

    public interface NoMimicResultListener {
        void onNoMimicDismiss(boolean launchSettings);
    }
}
