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

    public static void setAlarms(Context context) {
        List<Alarm> alarms =  AlarmList.get(context).getAlarms();
        for (Alarm alarm : alarms) {
            if (alarm.isEnabled()) {
                AlarmScheduler.scheduleAlarm(context, alarm);
            }
        }
    }

    public static void cancelAlarms(Context context) {
        List<Alarm> alarms =  AlarmList.get(context).getAlarms();
        for (Alarm alarm : alarms) {
            if (alarm.isEnabled()) {
                AlarmScheduler.cancelAlarm(context, alarm);
            }
        }
    }

    private static void refreshAlarms(Context context) {
        cancelAlarms(context);
        setAlarms(context);
    }


}
