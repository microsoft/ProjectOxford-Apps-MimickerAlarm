package com.microsoft.smartalarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.List;

public class AlarmRegistrar extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        refreshAlarms(context);
    }

    private static void refreshAlarms(Context context) {
        AlarmScheduler.cancelAlarms(context);
        AlarmScheduler.setAlarms(context);
    }
}
