package com.microsoft.smartalarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmRegistrar extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        refreshAlarms(context);
    }

    private static void refreshAlarms(Context context) {
        AlarmScheduler.cancelAlarms(context);
        AlarmScheduler.scheduleAlarms(context);
    }
}
