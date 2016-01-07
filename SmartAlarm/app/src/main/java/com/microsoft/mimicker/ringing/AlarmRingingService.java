package com.microsoft.mimicker.ringing;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.microsoft.mimicker.scheduling.AlarmNotificationManager;
import com.microsoft.mimicker.utilities.SharedWakeLock;
import com.microsoft.mimicker.utilities.Util;

import java.util.UUID;

public class AlarmRingingService extends Service {

    public final String TAG = this.getClass().getSimpleName();
    public static final String ACTION_START_FOREGROUND =
            "com.microsoft.mimicker.ringing.AlarmRingingService.START_FOREGROUND";
    public static final String ACTION_STOP_FOREGROUND =
            "com.microsoft.mimicker.ringing.AlarmRingingService.STOP_FOREGROUND";
    public static final String ACTION_DISPATCH_ALARM =
            "com.microsoft.mimicker.ringing.AlarmRingingService.DISPATCH_ALARM";
    public static final String ACTION_TOGGLE_WAKELOCK =
            "com.microsoft.mimicker.ringing.AlarmRingingService.TOGGLE_WAKELOCK";

    public static final String ALARM_ID = "alarm_id";
    private static final String ALARM_TIME = "alarm_time";
    private static final String WAKELOCK_ENABLE = "wakelock_enable";

    AlarmRingingDispatcher mDispatcher;
    private final IBinder mBinder = new LocalBinder();

    public static void startForegroundService(Context context,
                                              UUID alarmId,
                                              long alarmTime,
                                              boolean wakelockEnable) {
        Intent serviceIntent = new Intent(AlarmRingingService.ACTION_START_FOREGROUND);
        serviceIntent.setClass(context, AlarmRingingService.class);
        serviceIntent.putExtra(ALARM_ID, alarmId);
        serviceIntent.putExtra(ALARM_TIME, alarmTime);
        serviceIntent.putExtra(WAKELOCK_ENABLE, wakelockEnable);
        context.startService(serviceIntent);
    }

    public static void stopForegroundService(Context context) {
        Intent serviceIntent = new Intent(AlarmRingingService.ACTION_STOP_FOREGROUND);
        serviceIntent.setClass(context, AlarmRingingService.class);
        context.startService(serviceIntent);
    }

    public static void toggleWakeLock(Context context, boolean wakelockEnable) {
        Intent serviceIntent = new Intent(AlarmRingingService.ACTION_TOGGLE_WAKELOCK);
        serviceIntent.setClass(context, AlarmRingingService.class);
        serviceIntent.putExtra(WAKELOCK_ENABLE, wakelockEnable);
        context.startService(serviceIntent);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Util.registerCrashReport(this);

        Log.d(TAG, "Alarm service created!");

        mDispatcher = new AlarmRingingDispatcher(getApplicationContext());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Alarm service started! - OnStartCommand()");
        if (intent != null) {
            if (ACTION_DISPATCH_ALARM.equals(intent.getAction())) {
                Log.d(TAG, "Schedule ringing action!");
                mDispatcher.register(intent);
                AlarmWakeReceiver.completeWakefulIntent(intent);
            } else if (ACTION_START_FOREGROUND.equals(intent.getAction())) {
                Log.d(TAG, "Show active notification!");
                enableForegroundService(intent);
            } else if (ACTION_STOP_FOREGROUND.equals(intent.getAction())) {
                Log.d(TAG, "Remove active notification");
                disableForegroundService();
            } else if (ACTION_TOGGLE_WAKELOCK.equals(intent.getAction())) {
                Log.d(TAG, "Toggle wakelock!");
                toggleWakeLock(intent.getBooleanExtra(WAKELOCK_ENABLE, false));
            }
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Alarm service destroyed!");
    }

    public void reportAlarmRingingCompleted() {
        Log.d(TAG, "Alarm ring completed!");
        mDispatcher.workCompleted();
    }

    public class LocalBinder extends Binder {
        public AlarmRingingService getService() {
            return AlarmRingingService.this;
        }
    }

    private void enableForegroundService(Intent intent) {
        UUID alarmId = (UUID) intent.getSerializableExtra(ALARM_ID);
        long alarmTime = intent.getLongExtra(ALARM_TIME, 0);
        startForeground(AlarmNotificationManager.NOTIFICATION_ID,
                AlarmNotificationManager.createNotification(this, alarmId, alarmTime));
        toggleWakeLock(intent.getBooleanExtra(WAKELOCK_ENABLE, false));
    }

    private void disableForegroundService() {
        stopForeground(true);
        toggleWakeLock(false);
    }

    private void toggleWakeLock(boolean enableWakeLock) {
        if (enableWakeLock) {
            SharedWakeLock.get(this).acquirePartialWakeLock();
        } else {
            SharedWakeLock.get(this).releasePartialWakeLock();
        }
    }
}
