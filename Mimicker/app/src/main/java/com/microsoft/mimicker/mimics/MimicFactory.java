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

package com.microsoft.mimicker.mimics;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.microsoft.mimicker.model.Alarm;
import com.microsoft.mimicker.model.AlarmList;
import com.microsoft.mimicker.utilities.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * simple class that spawns a random mimic game while respect that mimics enabled in settings
 *
 * if no internet access is detected, spawns the NoNetwork mimic.
 */
public final class MimicFactory {

    public static final String MIMIC_FRAGMENT_TAG = "mimic_fragment";
    private static final String TAG = "MimicFactory";

    public static Fragment getMimicFragment(Activity caller, UUID alarmId) {
        Alarm alarm = AlarmList.get(caller).getAlarm(alarmId);
        List<Class> mimics = new ArrayList<>();

        if (alarm.isTongueTwisterEnabled()) {
            mimics.add(MimicTongueTwisterFragment.class);
        }
        if (alarm.isColorCaptureEnabled()) {
            mimics.add(MimicColorCaptureFragment.class);
        }
        if (alarm.isExpressYourselfEnabled()) {
            mimics.add(MimicExpressYourselfFragment.class);
        }

        Class mimic = null;
        if (mimics.size() > 0) {
            if (isNetworkAvailable(caller)) {
                int rand = new Random().nextInt(mimics.size());
                mimic = mimics.get(rand);
            }
            else {
                mimic = MimicNoNetworkFragment.class;
            }
        }

        Fragment fragment = null;
        if (mimic != null) {
            try {
                fragment = (Fragment) mimic.newInstance();
            } catch (Exception e) {
                Log.e(TAG, "Couldn't create fragment:", e);
                Logger.trackException(e);
            }
        }
        return fragment;
    }

    private static boolean isNetworkAvailable(Activity caller) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) caller.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public interface MimicResultListener {
        void onMimicSuccess(String shareable);

        void onMimicFailure();
    }
}
