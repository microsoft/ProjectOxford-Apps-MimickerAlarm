package com.microsoft.mimicker.scheduling;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmRegistrar extends BroadcastReceiver {

    private static void refreshAlarms(Context context) {
        AlarmScheduler.cancelAlarms(context);
        if (AlarmScheduler.scheduleAlarms(context)) {
            AlarmNotificationManager.get(context).handleAlarmNotificationStatus();
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        refreshAlarms(context);
    }
}
