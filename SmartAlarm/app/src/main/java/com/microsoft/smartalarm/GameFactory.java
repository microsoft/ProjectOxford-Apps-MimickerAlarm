package com.microsoft.smartalarm;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public final class GameFactory {

    public static final String SHAREABLE_URI = "shareable-uri";
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

        Class game = GameNoNetworkFragment.class;
        if (games.size() > 0 &&
                isNetworkAvailable(caller)) {
                int rand = new Random().nextInt(games.size());
            game = games.get(rand);
        }

        Fragment fragment = null;
        try {
            fragment = (Fragment) game.newInstance();
        } catch (Exception e) {
            Log.e(TAG, "Couldn't create fragment:", e);
        }
        return fragment;
    }

    private static boolean isNetworkAvailable(Activity caller) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) caller.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
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

    public interface GameResultListener {
        void onGameSuccess();

        void onGameFailure();
    }
}
