/*
 *
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license.
 *
 * Project Oxford: http://ProjectOxford.ai
 *
 * Project Oxford Mimicker Alarm Github:
 * https://github.com/Microsoft/ProjectOxford-Apps-MimickerAlarm
 *
 * Copyright (c) Microsoft Corporation
 * All rights reserved.
 *
 * MIT License:
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED ""AS IS"", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package com.microsoft.mimickeralarm.ringing;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.microsoft.mimickeralarm.scheduling.AlarmNotificationManager;
import com.microsoft.mimickeralarm.utilities.SharedWakeLock;
import com.microsoft.mimickeralarm.utilities.GeneralUtilities;

import java.util.UUID;

/**
 * This class is the main application Service that handles the following:
 *
 *  Hosting the AlarmRingingController which receives inbound intents with the DISPATCH_ALARM
 *  action.  These intents are sent from the AlarmWakeReceiver.
 *
 *  Transitioning to a foreground service to host the appropriate alarm notifications when an intent
 *  with the START_FOREGROUND action is received. These intents are sent from the
 *  AlarmNotificationManager.
 *
 *  Toggling the partial wakelock to try to enable better alarm reliability when an intent with the
 *  TOOGLE_WAKELOCK action is received. These intents are sent from the AlarmNotificationManager.
 *
 *  Transition from a foreground service back to a standard sticky service when an intent with the
 *  STOP_FOREGROUND action is received. These intents are sent from the AlarmNotificationManager.
 *
 * The service is bound to by the AlarmRingingActivity and it proxies calls to the hosted
 * AlarmRingingController.
 */
public class AlarmRingingService extends Service {

    public static final String ACTION_START_FOREGROUND =
            "com.microsoft.mimickeralarm.ringing.AlarmRingingService.START_FOREGROUND";
    public static final String ACTION_STOP_FOREGROUND =
            "com.microsoft.mimickeralarm.ringing.AlarmRingingService.STOP_FOREGROUND";
    public static final String ACTION_DISPATCH_ALARM =
            "com.microsoft.mimickeralarm.ringing.AlarmRingingService.DISPATCH_ALARM";
    public static final String ACTION_TOGGLE_WAKELOCK =
            "com.microsoft.mimickeralarm.ringing.AlarmRingingService.TOGGLE_WAKELOCK";
    public static final String ALARM_ID = "alarm_id";
    private static final String ALARM_TIME = "alarm_time";
    private static final String WAKELOCK_ENABLE = "wakelock_enable";
    private static final String NOTIFICATION_TYPE = "notification_type";
    public final String TAG = this.getClass().getSimpleName();

    private final IBinder mBinder = new LocalBinder();
    AlarmRingingController mController;

    public static void startForegroundService(Context context,
                                              UUID alarmId,
                                              long alarmTime,
                                              String notificationType) {
        Intent serviceIntent = new Intent(AlarmRingingService.ACTION_START_FOREGROUND);
        serviceIntent.setClass(context, AlarmRingingService.class);
        serviceIntent.putExtra(ALARM_ID, alarmId);
        serviceIntent.putExtra(ALARM_TIME, alarmTime);
        serviceIntent.putExtra(NOTIFICATION_TYPE, notificationType);
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

        GeneralUtilities.registerCrashReport(this);

        Log.d(TAG, "Alarm service created!");

        mController = AlarmRingingController.newInstance(getApplicationContext());
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
                mController.registerAlarm(intent);
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

    private void enableForegroundService(Intent intent) {
        UUID alarmId = (UUID) intent.getSerializableExtra(ALARM_ID);
        String notificationType = intent.getStringExtra(NOTIFICATION_TYPE);
        if (notificationType.equalsIgnoreCase(AlarmNotificationManager.NOTIFICATION_NEXT_ALARM)) {
            long alarmTime = intent.getLongExtra(ALARM_TIME, 0);
            startForeground(AlarmNotificationManager.NOTIFICATION_ID,
                    AlarmNotificationManager.createNextAlarmNotification(this, alarmId, alarmTime));
        } else if (notificationType.equalsIgnoreCase(AlarmNotificationManager.NOTIFICATION_ALARM_RUNNING)) {
            startForeground(AlarmNotificationManager.NOTIFICATION_ID,
                    AlarmNotificationManager.createAlarmRunningNotification(this, alarmId));
        }
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

    public void reportAlarmUXCompleted() {
        Log.d(TAG, "Alarm UX completed!");
        mController.alarmRingingSessionCompleted();
    }

    public void reportAlarmUXDismissed() {
        Log.d(TAG, "Alarm UX dismissed!");
        mController.alarmRingingSessionDismissed();
    }

    public void requestAllowUXDismiss() {
        Log.d(TAG, "Allow Dismiss UX requested!");
        mController.requestAllowDismiss();
    }

    public void silenceAlarmRinging() {
        Log.d(TAG, "Alarm silenced!");
        mController.silenceAlarmRinging();
    }

    public void startAlarmRinging() {
        Log.d(TAG, "Alarm restarted!");
        mController.startAlarmRinging();
    }

    public class LocalBinder extends Binder {
        public AlarmRingingService getService() {
            return AlarmRingingService.this;
        }
    }
}
