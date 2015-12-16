package com.microsoft.smartalarm;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import net.hockeyapp.android.CrashManager;

public class AlarmRingingService extends Service {

    public final String TAG = this.getClass().getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        final String hockeyappToken = Util.getToken(this, "hockeyapp");
        CrashManager.register(this, hockeyappToken);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Alarm service started!");
        SharedWakeLock.get(getApplicationContext()).acquireWakeLock();

        Intent alarmIntent = new Intent(getBaseContext(), AlarmRingingActivity.class);
        alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        alarmIntent.putExtras(intent.getExtras());
        getApplication().startActivity(alarmIntent);
        AlarmWakeReceiver.completeWakefulIntent(intent);
        return START_NOT_STICKY; // This guarantees we aren't restarted with a null intent
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Alarm service destroyed!");
    }
}
