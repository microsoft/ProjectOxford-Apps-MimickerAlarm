package com.microsoft.smartalarm;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class AlarmList {
    private static AlarmList sAlarmList;

    private List<Alarm> mAlarms;

    public static AlarmList get(Context context){
        if (sAlarmList == null) {
            sAlarmList = new AlarmList(context);
        }
        return sAlarmList;
    }

    private AlarmList (Context context) {
        mAlarms = new ArrayList<>();
        for (int i = 0; i < 20; i++ ) {
            Alarm alarm = new Alarm();
            alarm.setTitle("Alarm #" + i);
            alarm.setIsEnabled(i % 2 == 0);
            mAlarms.add(alarm);
        }
    }

    public List<Alarm> getAlarms() {
        return mAlarms;
    }

    public Alarm getAlarm(UUID id) {
        for (Alarm alarm : mAlarms) {
            if (alarm.getId().equals(id)){
                return alarm;
            }
        }
        return null;
    }
}
