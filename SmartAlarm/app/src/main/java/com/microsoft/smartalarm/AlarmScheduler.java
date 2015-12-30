package com.microsoft.smartalarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;
import java.util.List;

public final class AlarmScheduler {

    private AlarmScheduler() {}

    public static final String ALARM_ID = "alarm_id";

    public static void setAlarms(Context context) {
        List<Alarm> alarms =  AlarmList.get(context).getAlarms();
        for (Alarm alarm : alarms) {
            if (alarm.isEnabled()) {
                scheduleAlarm(context, alarm);
            }
        }
    }

    public static void cancelAlarms(Context context) {
        List<Alarm> alarms =  AlarmList.get(context).getAlarms();
        for (Alarm alarm : alarms) {
            if (alarm.isEnabled()) {
                cancelAlarm(context, alarm);
            }
        }
    }

    public static void scheduleAlarm(Context context, Alarm alarm) {
        if (alarm.isOneShot()) {
            scheduleOneShot(context, alarm);
        } else {
            scheduleRepeating(context, alarm);
        }
    }

    public static void snoozeAlarm(Context context, Alarm alarm, int snoozePeriod) {
        Calendar calendarAlarm = Calendar.getInstance();
        long now = calendarAlarm.getTimeInMillis();
        calendarAlarm.setTimeInMillis(now + snoozePeriod);
        PendingIntent pendingIntent = createPendingIntent(context, alarm);
        setAlarm(context, calendarAlarm, pendingIntent);
    }

    public static void cancelAlarm(Context context, Alarm alarm) {
        PendingIntent pIntent = createPendingIntent(context, alarm);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pIntent);
    }

    private static void scheduleOneShot(Context context, Alarm alarm) {
        Calendar calendarAlarm = Calendar.getInstance();
        calendarAlarm.set(Calendar.HOUR_OF_DAY, alarm.getTimeHour());
        calendarAlarm.set(Calendar.MINUTE, alarm.getTimeMinute());
        calendarAlarm.set(Calendar.SECOND, 0);

        final int nowHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        final int nowMinute = Calendar.getInstance().get(Calendar.MINUTE);
        final int nowDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);

        // if we cannot schedule today then set the alarm for tomorrow
        if ((alarm.getTimeHour() < nowHour) ||
            (alarm.getTimeHour() == nowHour && alarm.getTimeMinute() <= nowMinute)) {
            calendarAlarm.set(Calendar.DAY_OF_WEEK, getNextWeekday(nowDay));
        }

        PendingIntent pendingIntent = createPendingIntent(context, alarm);
        setAlarm(context, calendarAlarm, pendingIntent);
    }

    private static void scheduleRepeating(Context context, Alarm alarm) {
        Calendar calendarAlarm = Calendar.getInstance();
        calendarAlarm.set(Calendar.HOUR_OF_DAY, alarm.getTimeHour());
        calendarAlarm.set(Calendar.MINUTE, alarm.getTimeMinute());
        calendarAlarm.set(Calendar.SECOND, 0);
        boolean thisWeek = false;

        final int nowDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        final int nowHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        final int nowMinute = Calendar.getInstance().get(Calendar.MINUTE);

        // First check if it's later in the week
        for (int dayOfWeek = Calendar.SUNDAY; dayOfWeek <= Calendar.SATURDAY; ++dayOfWeek) {
            if (alarm.getRepeatingDay(dayOfWeek - 1) && dayOfWeek >= nowDay &&
                    !(dayOfWeek == nowDay && alarm.getTimeHour() < nowHour) &&
                    !(dayOfWeek == nowDay && alarm.getTimeHour() == nowHour && alarm.getTimeMinute() <= nowMinute)) {
                calendarAlarm.set(Calendar.DAY_OF_WEEK, dayOfWeek);
                thisWeek = true;
                break;
            }
        }

        if (!thisWeek) {
            for (int dayOfWeek = Calendar.SUNDAY; dayOfWeek <= Calendar.SATURDAY; ++dayOfWeek) {
                if (alarm.getRepeatingDay(dayOfWeek - 1) && dayOfWeek <= nowDay) {
                    calendarAlarm.set(Calendar.DAY_OF_WEEK, dayOfWeek);
                    calendarAlarm.add(Calendar.WEEK_OF_YEAR, 1);
                    break;
                }
            }
        }

        PendingIntent pendingIntent = createPendingIntent(context, alarm);
        // TODO Ensure we make repeating beyond a week
        setAlarm(context, calendarAlarm, pendingIntent);
    }

    private static PendingIntent createPendingIntent(Context context, Alarm alarm) {
        Intent intent = new Intent(context, AlarmWakeReceiver.class);
        intent.putExtra(ALARM_ID, alarm.getId());

        return PendingIntent.getBroadcast(context, (int) alarm.getId().getLeastSignificantBits(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static void setAlarm(Context context, Calendar calendar, PendingIntent pIntent) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pIntent);
        }
    }

    private static int getNextWeekday(int day) {
        if (day == Calendar.SATURDAY) {
            return Calendar.SUNDAY;
        } else {
            return day + 1;
        }
    }
}
