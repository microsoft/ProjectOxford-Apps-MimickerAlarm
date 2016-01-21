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

package com.microsoft.mimickeralarm.utilities;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.microsoft.mimickeralarm.R;
import com.microsoft.mimickeralarm.settings.AlarmSettingsFragment;
import com.microsoft.mimickeralarm.settings.MimicsSettingsFragment;

import java.util.ArrayList;

/**
 * This utility class groups together common Settings user experience functionality which is
 * utilized from within both the AlarmMainActivity and AlarmRingingActivity.
 */
public final class SettingsUtilities {
    private SettingsUtilities() {}

    public static AlarmSettingsFragment getAlarmSettingsFragment(FragmentManager fragmentManager) {
        return (AlarmSettingsFragment)fragmentManager
                .findFragmentByTag(AlarmSettingsFragment.SETTINGS_FRAGMENT_TAG);
    }

    public static MimicsSettingsFragment getMimicsSettingsFragment(FragmentManager fragmentManager) {
        return (MimicsSettingsFragment)fragmentManager
                .findFragmentByTag(MimicsSettingsFragment.MIMICS_SETTINGS_FRAGMENT_TAG);
    }

    public static boolean areEditingAlarmSettingsExclusive(FragmentManager fragmentManager) {
        return (getAlarmSettingsFragment(fragmentManager) != null) &&
                (getMimicsSettingsFragment(fragmentManager) == null);
    }

    public static boolean areEditingMimicsSettingsExclusive(FragmentManager fragmentManager) {
        return (getAlarmSettingsFragment(fragmentManager) == null) &&
                (getMimicsSettingsFragment(fragmentManager) != null);
    }

    public static boolean areEditingSettings(FragmentManager fragmentManager) {
        return (getAlarmSettingsFragment(fragmentManager) != null) ||
                (getMimicsSettingsFragment(fragmentManager) != null);
    }

    public static void transitionFromAlarmToMimicsSettings(FragmentManager fragmentManager,
                                                           ArrayList<String> enabledMimics) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                R.anim.slide_in_left, R.anim.slide_out_right);
        transaction.replace(R.id.fragment_container, MimicsSettingsFragment.newInstance(enabledMimics),
                MimicsSettingsFragment.MIMICS_SETTINGS_FRAGMENT_TAG);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public static void transitionFromAlarmListToSettings(FragmentManager fragmentManager,
                                                           String alarmId) {
        GeneralUtilities.showFragmentFromRight(fragmentManager,
                AlarmSettingsFragment.newInstance(alarmId),
                AlarmSettingsFragment.SETTINGS_FRAGMENT_TAG);
    }
}
