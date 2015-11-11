package com.microsoft.smartalarm;

import android.database.Cursor;
import android.database.CursorWrapper;
import android.net.Uri;

import com.microsoft.smartalarm.AlarmDbSchema.AlarmTable;

import java.util.UUID;

public class AlarmCursorWrapper extends CursorWrapper {
    public AlarmCursorWrapper(Cursor cursor) { super(cursor); }

    public Alarm getAlarm() {
        String uuidString = getString(getColumnIndex(AlarmTable.Columns.UUID));
        String title = getString(getColumnIndex(AlarmTable.Columns.TITLE));
        boolean isEnabled = (getInt(getColumnIndex(AlarmTable.Columns.ENABLED)) != 0);
        int timeHour = getInt(getColumnIndex(AlarmTable.Columns.HOUR));
        int timeMinute = getInt(getColumnIndex(AlarmTable.Columns.MINUTE));
        boolean repeatWeekly = (getInt(getColumnIndex(AlarmTable.Columns.WEEKLY)) != 0);
        String alarmToneString = getString(getColumnIndex(AlarmTable.Columns.TONE));
        Uri alarmTone = null;
        if (!alarmToneString.isEmpty()) {
            alarmTone = Uri.parse(alarmToneString);
        }
        String[] repeatingDays = getString(getColumnIndex(AlarmTable.Columns.DAYS)).split(",");


        Alarm alarm = new Alarm(UUID.fromString(uuidString));
        alarm.setTitle(title);
        alarm.setIsEnabled(isEnabled);
        alarm.setTimeHour(timeHour);
        alarm.setTimeMinute(timeMinute);
        alarm.setRepeatWeekly(repeatWeekly);
        alarm.setAlarmTone(alarmTone);
        for (int i = 0; i <repeatingDays.length; i++) {
            alarm.setRepeatingDay(i, !repeatingDays[i].equals("false"));
        }

        return alarm;
    }
}
