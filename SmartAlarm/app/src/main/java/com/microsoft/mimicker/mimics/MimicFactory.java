package com.microsoft.mimicker.mimics;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.microsoft.mimicker.model.Alarm;
import com.microsoft.mimicker.model.AlarmList;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public final class MimicFactory {

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
