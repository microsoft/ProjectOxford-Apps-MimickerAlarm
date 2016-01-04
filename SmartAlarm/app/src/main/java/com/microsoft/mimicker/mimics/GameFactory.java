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

public final class GameFactory {

    private static final String TAG = "GameFactory";

    public static Fragment getGameFragment(Activity caller, UUID alarmId) {
        Alarm alarm = AlarmList.get(caller).getAlarm(alarmId);
        List<Class> games = new ArrayList<>();

        if (alarm.isTongueTwisterEnabled()) {
            games.add(GameTwisterFragment.class);
        }
        if (alarm.isColorCollectorEnabled()) {
            games.add(GameColorFinderFragment.class);
        }
        if (alarm.isExpressYourselfEnabled()) {
            games.add(GameEmotionFragment.class);
        }

        Class game = null;
        if (games.size() > 0) {
            if (isNetworkAvailable(caller)) {
                int rand = new Random().nextInt(games.size());
                game = games.get(rand);
            }
            else {
                game = GameNoNetworkFragment.class;
            }
        }

        Fragment fragment = null;
        if (game != null) {
            try {
                fragment = (Fragment) game.newInstance();
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

    public interface GameResultListener {
        void onGameSuccess(String shareable);

        void onGameFailure();
    }
}
