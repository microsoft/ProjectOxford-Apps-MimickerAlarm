package com.microsoft.smartalarm;

import android.app.Activity;
import android.content.Intent;

import java.util.Random;

public final class GameFactory {
    private static Class[] sGames = new Class[]{ GameTwister.class,
                                                 GameColorFinderActivity.class};

    public static final int START_GAME_REQUEST = 1;

    public static void startRandom(Activity caller) {
        int rand = new Random().nextInt(sGames.length);
        Class game = sGames[rand];
        Intent intent = new Intent(caller, game);
        caller.startActivityForResult(intent, START_GAME_REQUEST);
    }
}
