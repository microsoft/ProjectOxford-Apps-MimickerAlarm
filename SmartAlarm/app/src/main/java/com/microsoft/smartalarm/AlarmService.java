package com.microsoft.smartalarm;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import net.hockeyapp.android.CrashManager;

public class AlarmService extends Service {
    @Override
    public void onCreate() {
        super.onCreate();
        final String hockeyAppId = getResources().getString(R.string.hockeyapp_id);
        CrashManager.register(this, hockeyAppId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // When we restart after being killed we get a null intent
        if (intent != null) {
            Intent alarmIntent = new Intent(getBaseContext(), AlarmRingingActivity.class);
            alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            alarmIntent.putExtras(intent);
            getApplication().startActivity(alarmIntent);
        }

        //AlarmManagerHelper.setAlarms(this);

        return START_STICKY;
    }

}
