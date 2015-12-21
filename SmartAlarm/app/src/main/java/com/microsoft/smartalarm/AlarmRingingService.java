package com.microsoft.smartalarm;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import net.hockeyapp.android.CrashManager;

public class AlarmRingingService extends Service {

    public final String TAG = this.getClass().getSimpleName();

    AlarmRingingDispatcher mDispatcher;
    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        AlarmRingingService getService() {
            return AlarmRingingService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Alarm service created!");

        final String hockeyappToken = Util.getToken(this, "hockeyapp");
        CrashManager.register(this, hockeyappToken);

        mDispatcher = new AlarmRingingDispatcher(getApplicationContext());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Alarm service started!");
        mDispatcher.register(intent);
        AlarmWakeReceiver.completeWakefulIntent(intent);
        return START_NOT_STICKY; // This guarantees we aren't restarted with a null intent
    }

    public void reportAlarmRingingCompleted() {
        mDispatcher.workCompleted();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Alarm service destroyed!");
    }
}
