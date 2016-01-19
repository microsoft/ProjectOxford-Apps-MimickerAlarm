package com.microsoft.mimicker.utilities;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.microsoft.mimicker.R;
import com.microsoft.mimicker.settings.AlarmSettingsFragment;
import com.microsoft.mimicker.settings.MimicsSettingsFragment;

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
