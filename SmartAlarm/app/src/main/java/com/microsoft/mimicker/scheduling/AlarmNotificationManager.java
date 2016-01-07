package com.microsoft.mimicker.scheduling;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.NotificationCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import com.ibm.icu.text.MessageFormat;
import com.microsoft.mimicker.R;
import com.microsoft.mimicker.appcore.AlarmMainActivity;
import com.microsoft.mimicker.model.Alarm;
import com.microsoft.mimicker.model.AlarmList;
import com.microsoft.mimicker.ringing.AlarmRingingService;
import com.microsoft.mimicker.utilities.AlarmUtils;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;

public class AlarmNotificationManager {
    public final static int NOTIFICATION_ID = 60653426;

    private static final String TAG = "AlarmNotificationMgr";
    private static AlarmNotificationManager sManager;

    private Context mContext;
    private UUID mCurrentAlarmId;
    private long mCurrentAlarmTime;
    private boolean mNotificationsActive;
    private boolean mWakeLockEnable;

    private AlarmNotificationManager(Context context) {
        mContext = context;
        resetState();
    }

    public static AlarmNotificationManager get(Context context) {
        if (sManager == null) {
            sManager = new AlarmNotificationManager(context);
            Log.d(TAG, "Initialized!");
        }
        return sManager;
    }

    public static Notification createNotification(Context context, UUID alarmId, long alarmTime) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.ic_alarm_on_white_18dp);
        Bitmap icon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher_no_bg);
        builder.setLargeIcon(icon);

        String alarmDisplayString = AlarmUtils.getDateAndTimeAlarmDisplayString(context, alarmTime);
        builder.setContentText(context.getString(R.string.notification_content));
        builder.setContentTitle(context.getString(R.string.app_name));
        Map<String, String> args = new HashMap<>();
        args.put("time", alarmDisplayString);
        String ticker = new MessageFormat(context.getString(R.string.notification_ticker)).format(args);
        builder.setTicker(ticker);
        builder.setSubText(alarmDisplayString);

        Intent startIntent = new Intent(context, AlarmMainActivity.class);
        startIntent.putExtra(AlarmRingingService.ALARM_ID, alarmId);
        PendingIntent contentIntent = PendingIntent.getActivity(context, (int)Math.abs(alarmId.getLeastSignificantBits()), startIntent, 0);
        builder.setContentIntent(contentIntent);
        return builder.build();
    }

    public void handleAlarmNotificationStatus() {
        // Check if notifications are enabled
        if (!shouldEnableNotifications()) return;

        // Find the alarm that will fire next
        List<Alarm> alarms = AlarmList.get(mContext).getAlarms();
        Calendar now = Calendar.getInstance();
        SortedMap<Long, UUID> alarmValues = new TreeMap<>();
        for (Alarm alarm : alarms) {
            if (alarm.isEnabled()) {
                alarmValues.put(AlarmScheduler.getTimeUntilAlarm(now, alarm), alarm.getId());
            }
        }

        //  We will now decide whether we need to do nothing, update or remove the notification
        if (!alarmValues.isEmpty()) {
            Long alarmTime = alarmValues.firstKey();
            UUID alarmId = alarmValues.get(alarmTime);
            boolean wakelockEnable = shouldEnableWakeLock();
            if (!doesCurrentStateMatchAlarmDetails(alarmId, alarmTime, wakelockEnable)) {
                updateStateWithAlarmDetails(alarmId, alarmTime, wakelockEnable);
                AlarmRingingService.startForegroundService(mContext,
                        mCurrentAlarmId,
                        mCurrentAlarmTime,
                        mWakeLockEnable);
            }
        } else {
            disableNotifications();
        }
    }

    public void disableNotifications() {
        if (mNotificationsActive) {
            AlarmRingingService.stopForegroundService(mContext);
            resetState();
        }
    }

    public void toggleWakeLock(boolean wakelockEnable) {
        if (mNotificationsActive) {
            mWakeLockEnable = wakelockEnable;
            AlarmRingingService.toggleWakeLock(mContext, mWakeLockEnable);
        }
    }

    private void updateStateWithAlarmDetails(UUID alarmId, long alarmTime, boolean wakelockEnable) {
        mNotificationsActive = true;
        mCurrentAlarmId = alarmId;
        mCurrentAlarmTime = alarmTime;
        mWakeLockEnable = wakelockEnable;
    }

    private boolean doesCurrentStateMatchAlarmDetails(UUID alarmId, long alarmTime, boolean wakelockEnable) {
        return (mCurrentAlarmTime == alarmTime &&
                mCurrentAlarmId.equals(alarmId) &&
                mWakeLockEnable == wakelockEnable);
    }

    private void resetState() {
        mNotificationsActive = false;
        mCurrentAlarmId = new UUID(0, 0);
        mCurrentAlarmTime = 0;
        mWakeLockEnable = false;
    }

    private boolean shouldEnableNotifications() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        return preferences.getBoolean(mContext.getString(R.string.pref_enable_notifications_key), false);
    }

    private boolean shouldEnableWakeLock() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        return preferences.getBoolean(mContext.getString(R.string.pref_enable_reliability_key), false);
    }
}
