package com.microsoft.smartalarm;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public final class GameFactory {

    public static final int START_GAME_REQUEST = 1;

    public static boolean startGame(Activity caller, UUID alarmId) {
        Alarm alarm = AlarmList.get(caller).getAlarm(alarmId);

        List<Class> games = new ArrayList<>();
        if (alarm.isTongueTwisterEnabled()) {
            games.add(GameTwister.class);
        }
        if (alarm.isColorCollectorEnabled()) {
            games.add(GameColorFinderActivity.class);
        }
        if (alarm.isExpressYourselfEnabled()) {
            games.add(GameEmotionActivity.class);
        }

        if (games.size() > 0) {
            if (isNetworkAvailable(caller)) {
                int rand = new Random().nextInt(games.size());
                Class game = games.get(rand);
                Intent intent = new Intent(caller, game);
                caller.startActivityForResult(intent, START_GAME_REQUEST);
                return true;
            }
            else {
                noNetworkGame(caller);
                return true;
            }
        } else {
            return false;
        }
    }

    private static boolean isNetworkAvailable(Activity caller) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) caller.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static void noNetworkGame(Activity caller){
        Intent intent = new Intent(caller, GameNoNetwork.class);
        caller.startActivityForResult(intent, START_GAME_REQUEST);
    }
}
