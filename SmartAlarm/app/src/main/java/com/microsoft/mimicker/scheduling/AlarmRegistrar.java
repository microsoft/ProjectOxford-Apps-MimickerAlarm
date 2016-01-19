package com.microsoft.mimicker.scheduling;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * This BroadcastReceiver is registered to be called for the following system intents:
 *
 * Boot - when the system is booted we want to re-register the alarms with the AlarmManager
 * Time/Date/Timezone change - We re-register with the alarm manager when any of these changes happen
 * in the system.
 *
 * Note that this receiver is called in the main thread, so there should not be any thread safety
 * issues with interfering with the alarm user interface functionality.
 *
 * Once we have re-registered alarms, we check to see if we need to display a notification by
 * calling in the AlarmNotificationManager.
 */
public class AlarmRegistrar extends BroadcastReceiver {

    private static void refreshAlarms(Context context) {
        AlarmScheduler.cancelAlarms(context);
        if (AlarmScheduler.scheduleAlarms(context)) {
            AlarmNotificationManager.get(context).handleNextAlarmNotificationStatus();
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        refreshAlarms(context);
    }
}
