package com.microsoft.smartalarm;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public final class GameFactory {

    public static final int START_GAME_REQUEST = 1;
    public static final String SHAREABLE_URI = "shareable-uri";

    public static boolean startGame(Activity caller, UUID alarmId) {
        Alarm alarm = AlarmList.get(caller).getAlarm(alarmId);

        List<Class> games = new ArrayList<>();
        if (alarm.isTongueTwisterEnabled()) {
            games.add(GameTwisterFragment.class);
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

    public static Uri saveShareableBitmap(Context context, Bitmap bitmap) {
        File tempFile;
        try {
            tempFile = File.createTempFile("test", ".png", context.getCacheDir());
            tempFile.setReadable(true, false);
            tempFile.deleteOnExit();
        }
        catch (IOException ex) {
            Logger.trackException(ex);
            return null;
        }

        if (tempFile.canWrite()){
            try {
                FileOutputStream stream = new FileOutputStream(tempFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
                stream.close();
            }
            catch (IOException ex) {
                Logger.trackException(ex);
                return null;
            }
        }
        return Uri.fromFile(tempFile);
    }
}
