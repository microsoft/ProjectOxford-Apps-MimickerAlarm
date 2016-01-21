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

package com.microsoft.mimickeralarm.onboarding;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.microsoft.mimickeralarm.R;
import com.microsoft.mimickeralarm.appcore.AlarmMainActivity;
import com.microsoft.mimickeralarm.utilities.Loggable;
import com.microsoft.mimickeralarm.utilities.Logger;
import com.microsoft.mimickeralarm.utilities.GeneralUtilities;

public class OnboardingToSFragment extends Fragment {
    public static final String TOS_FRAGMENT_TAG = "tos_fragment";
    OnOnboardingToSListener mCallback;
    View mRootToSView;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallback = (OnOnboardingToSListener) context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Logger.init(getActivity());

        mRootToSView = inflater.inflate(R.layout.fragment_onboarding_tos, container, false);
        mRootToSView.findViewById(R.id.onboarding_tos_gotit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transitionToSPage(R.id.onboarding_tos_acceptance_before, R.id.onboarding_tos_acceptance);
            }
        });

        mRootToSView.findViewById(R.id.onboarding_tos_accept).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Loggable.UserAction userAction = new Loggable.UserAction(Loggable.Key.ACTION_ONBOARDING_TOS_ACCEPT);
                Logger.track(userAction);
                String packageName = getActivity().getApplication().getPackageName();
                SharedPreferences preferences = getActivity().getSharedPreferences(packageName, Context.MODE_PRIVATE);
                preferences.edit().putBoolean(AlarmMainActivity.SHOULD_TOS, false).apply();
                mCallback.onAccept();
            }
        });

        mRootToSView.findViewById(R.id.onboarding_tos_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Loggable.UserAction userAction = new Loggable.UserAction(Loggable.Key.ACTION_ONBOARDING_TOS_DECLINE);
                Logger.track(userAction);
                transitionToSPage(R.id.onboarding_tos_acceptance, R.id.onboarding_tos_acceptance_reminder);
            }
        });

        mRootToSView.findViewById(R.id.onboarding_tos_gonow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transitionToSPage(R.id.onboarding_tos_acceptance_reminder, R.id.onboarding_tos_acceptance);
            }
        });

        GeneralUtilities.enableLinks((TextView) mRootToSView.findViewById(R.id.onboarding_tos_text));
        return mRootToSView;
    }

    private void transitionToSPage(int resourceIdFromPage, int resourceIdToPage) {
        mRootToSView.findViewById(resourceIdFromPage).setVisibility(View.GONE);
        mRootToSView.findViewById(resourceIdToPage).setVisibility(View.VISIBLE);
    }

    public interface OnOnboardingToSListener {
        void onAccept();
    }
}

