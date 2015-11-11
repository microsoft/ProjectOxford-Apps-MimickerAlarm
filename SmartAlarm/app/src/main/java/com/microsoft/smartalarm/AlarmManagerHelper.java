package com.microsoft.smartalarm;

import java.util.Calendar;
import java.util.List;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmManagerHelper extends BroadcastReceiver {

    public static final String ID = "id";
    public static final String TITLE = "title";
    public static final String TIME_HOUR = "timeHour";
    public static final String TIME_MINUTE = "timeMinute";
    public static final String TONE = "alarmTone";

    @Override
    public void onReceive(Context context, Intent intent) {
        setAlarms(context);
    }

    public static void setAlarms(Context context) {
        cancelAlarms(context);

        List<Alarm> alarms =  AlarmList.get(context).getAlarms();

        for (Alarm alarm : alarms) {
            if (alarm.isEnabled()) {

                PendingIntent pIntent = createPendingIntent(context, alarm);

                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, alarm.getTimeHour());
                calendar.set(Calendar.MINUTE, alarm.getTimeMinute());
                calendar.set(Calendar.SECOND, 00);

                final int nowDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
                final int nowHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                final int nowMinute = Calendar.getInstance().get(Calendar.MINUTE);
                boolean alarmSet = false;

                // First check if it's later in the week
                for (int dayOfWeek = Calendar.SUNDAY; dayOfWeek <= Calendar.SATURDAY; ++dayOfWeek) {
                    if (alarm.getRepeatingDay(dayOfWeek - 1) && dayOfWeek >= nowDay &&
                            !(dayOfWeek == nowDay && alarm.getTimeHour() < nowHour) &&
                            !(dayOfWeek == nowDay && alarm.getTimeHour() == nowHour && alarm.getTimeMinute() <= nowMinute)) {
                        calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);

                        setAlarm(context, calendar, pIntent);
                        alarmSet = true;
                        break;
                    }
                }

                // Check if it's earlier in the week
                if (!alarmSet) {
                    for (int dayOfWeek = Calendar.SUNDAY; dayOfWeek <= Calendar.SATURDAY; ++dayOfWeek) {
                        if (alarm.getRepeatingDay(dayOfWeek - 1) && dayOfWeek <= nowDay && alarm.isRepeatWeekly()) {
                            calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);
                            calendar.add(Calendar.WEEK_OF_YEAR, 1);

                            setAlarm(context, calendar, pIntent);
                            break;
                        }
                    }
                }
            }
        }
    }

    private static void setAlarm(Context context, Calendar calendar, PendingIntent pIntent) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pIntent);
        }
    }

    public static void cancelAlarms(Context context) {
        List<Alarm> alarms =  AlarmList.get(context).getAlarms();

        if (alarms != null) {
            for (Alarm alarm : alarms) {
                if (alarm.isEnabled()) {
                    PendingIntent pIntent = createPendingIntent(context, alarm);

                    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                    alarmManager.cancel(pIntent);
                }
            }
        }
    }

    private static PendingIntent createPendingIntent(Context context, Alarm alarm) {
        Intent intent = new Intent(context, AlarmService.class);
        intent.putExtra(ID, alarm.getId());
        intent.putExtra(TITLE, alarm.getTitle());
        intent.putExtra(TIME_HOUR, alarm.getTimeHour());
        intent.putExtra(TIME_MINUTE, alarm.getTimeMinute());
        intent.putExtra(TONE, alarm.getAlarmTone().toString());

        return PendingIntent.getService(context, (int) alarm.getId().getLeastSignificantBits(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
