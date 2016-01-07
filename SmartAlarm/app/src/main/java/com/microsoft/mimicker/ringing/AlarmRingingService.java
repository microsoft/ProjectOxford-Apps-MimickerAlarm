package com.microsoft.mimicker.ringing;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.microsoft.mimicker.R;
import com.microsoft.mimicker.appcore.AlarmApplication;
import com.microsoft.mimicker.appcore.AlarmMainActivity;
import com.microsoft.mimicker.utilities.AlarmUtils;
import com.microsoft.mimicker.utilities.Util;

import java.util.UUID;

public class AlarmRingingService extends Service {

    public final String TAG = this.getClass().getSimpleName();
    public static final String ACTION_START_FOREGROUND =
            "com.microsoft.mimicker.ringing.AlarmRingingService.START_FOREGROUND";
    public static final String ACTION_END_FOREGROUND =
            "com.microsoft.mimicker.ringing.AlarmRingingService.END_FOREGROUND";
    public static final String ACTION_DISPATCH_ALARM =
            "com.microsoft.mimicker.ringing.AlarmRingingService.DISPATCH_ALARM";

    public static final String ALARM_ID = "alarm_id";
    public static final String ALARM_TIME = "alarm_time";
    private final static int NOTIFICATION_ID = 60653426;


    AlarmRingingDispatcher mDispatcher;
    private final IBinder mBinder = new LocalBinder();

    public static void sendAlarmNotification(Context context, UUID alarmId, long alarmTime) {
        Intent serviceIntent = new Intent(AlarmRingingService.ACTION_START_FOREGROUND);
        serviceIntent.setClass(context, AlarmRingingService.class);
        serviceIntent.putExtra(ALARM_ID, alarmId);
        serviceIntent.putExtra(ALARM_TIME, alarmTime);
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
        Log.d(TAG, "Alarm service started!");
        if (intent != null) {
            if (ACTION_DISPATCH_ALARM.equals(intent.getAction())) {
                Log.d(TAG, "Schedule ringing action!");
                mDispatcher.register(intent);
                AlarmWakeReceiver.completeWakefulIntent(intent);
            } else if (ACTION_START_FOREGROUND.equals(intent.getAction())) {
                Log.d(TAG, "Show active notification!");
                UUID alarmId = (UUID) intent.getSerializableExtra(ALARM_ID);
                long alarmTime = intent.getLongExtra(ALARM_TIME, 0);
                startForeground(NOTIFICATION_ID, buildNotification(alarmId, alarmTime));
            } else if (ACTION_END_FOREGROUND.equals(intent.getAction())) {
                Log.d(TAG, "Remove active notification");
                stopForeground(true);
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

    private Notification buildNotification(UUID alarmId, long alarmTime) {
        Context context = AlarmApplication.getAppContext();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.ic_alarm_on_white_18dp);
        Bitmap icon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher_no_bg);
        builder.setLargeIcon(icon);

        String alarmDisplayString = AlarmUtils.getDateAndTimeAlarmDisplayString(context, alarmTime);
        builder.setContentText("The next alarm will ring at");
        builder.setContentTitle("Mimicker Alarm");
        builder.setTicker("The next Mimicker alarm will ring at " + alarmDisplayString);
        builder.setSubText(alarmDisplayString);

        Intent startIntent = new Intent(context, AlarmMainActivity.class);
        startIntent.putExtra(ALARM_ID, alarmId);
        PendingIntent contentIntent = PendingIntent.getActivity(context, (int)Math.abs(alarmId.getLeastSignificantBits()), startIntent, 0);
        builder.setContentIntent(contentIntent);
        return builder.build();
    }
}
