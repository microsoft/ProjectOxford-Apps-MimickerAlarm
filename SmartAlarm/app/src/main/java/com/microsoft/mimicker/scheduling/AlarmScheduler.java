package com.microsoft.mimicker.scheduling;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.microsoft.mimicker.model.Alarm;
import com.microsoft.mimicker.model.AlarmList;
import com.microsoft.mimicker.ringing.AlarmWakeReceiver;

import java.util.Calendar;
import java.util.List;

public final class AlarmScheduler {

    public static final String ALARM_ID = "alarm_id";

    private AlarmScheduler() {
    }

    public static void scheduleAlarms(Context context) {
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

    public static long scheduleAlarm(Context context, Alarm alarm) {
        PendingIntent pendingIntent = createPendingIntent(context, alarm);
        Calendar calenderNow = Calendar.getInstance();
        long time = getTimeUntilAlarm(calenderNow, alarm);
        setAlarm(context, time, pendingIntent);
        return time;
    }

    public static long getTimeUntilAlarm(Calendar calendarFrom, Alarm alarm) {
        if (alarm.isOneShot()) {
            return getTimeUntilOneShotAlarm(calendarFrom, alarm);
        } else {
            return getTimeUntilRepeatingAlarm(calendarFrom, alarm);
        }
    }

    public static void snoozeAlarm(Context context, Alarm alarm, int snoozePeriod) {
        Calendar calendarAlarm = Calendar.getInstance();
        long now = calendarAlarm.getTimeInMillis();
        calendarAlarm.setTimeInMillis(now + snoozePeriod);
        PendingIntent pendingIntent = createPendingIntent(context, alarm);
        setAlarm(context, calendarAlarm.getTimeInMillis(), pendingIntent);
    }

    public static void cancelAlarm(Context context, Alarm alarm) {
        PendingIntent pIntent = createPendingIntent(context, alarm);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pIntent);
    }

    private static long getTimeUntilOneShotAlarm(Calendar calendarFrom, Alarm alarm) {
        Calendar calendarAlarm = Calendar.getInstance();
        calendarAlarm.set(Calendar.HOUR_OF_DAY, alarm.getTimeHour());
        calendarAlarm.set(Calendar.MINUTE, alarm.getTimeMinute());
        calendarAlarm.set(Calendar.SECOND, 0);

        final int nowHour = calendarFrom.get(Calendar.HOUR_OF_DAY);
        final int nowMinute = calendarFrom.get(Calendar.MINUTE);
<<<<<<< Updated upstream

        // if we cannot schedule today then set the alarm for tomorrow
        if ((alarm.getTimeHour() < nowHour) ||
            (alarm.getTimeHour() == nowHour && alarm.getTimeMinute() <= nowMinute)) {
=======
        final int nowSeconds = calendarFrom.get(Calendar.SECOND);

        int alarmHour = includeSnoozed ? alarm.getSnoozeHour() : alarm.getTimeHour();
        int alarmMinute = includeSnoozed ? alarm.getSnoozeMinute() : alarm.getTimeMinute();
        int alarmSeconds = includeSnoozed ? alarm.getSnoozeSeconds() : 0;

        // if we cannot schedule today then set the alarm for tomorrow
        if ((alarmHour < nowHour) ||
            (alarmHour == nowHour && alarmMinute < nowMinute) ||
            (alarmHour == nowHour && alarmMinute == nowMinute &&
                    alarmSeconds <= nowSeconds)) {
>>>>>>> Stashed changes
            calendarAlarm.add(Calendar.DATE, 1);
        }

        return calendarAlarm.getTimeInMillis();
    }

    private static long getTimeUntilRepeatingAlarm(Calendar calendarFrom, Alarm alarm) {
        Calendar calendarAlarm = Calendar.getInstance();
        calendarAlarm.set(Calendar.HOUR_OF_DAY, alarm.getTimeHour());
        calendarAlarm.set(Calendar.MINUTE, alarm.getTimeMinute());
        calendarAlarm.set(Calendar.SECOND, 0);
        boolean thisWeek = false;

        final int nowDay = calendarFrom.get(Calendar.DAY_OF_WEEK);
        final int nowHour = calendarFrom.get(Calendar.HOUR_OF_DAY);
        final int nowMinute = calendarFrom.get(Calendar.MINUTE);
<<<<<<< Updated upstream
=======
        final int nowSeconds = calendarFrom.get(Calendar.SECOND);

        int alarmHour = includeSnoozed ? alarm.getSnoozeHour() : alarm.getTimeHour();
        int alarmMinute = includeSnoozed ? alarm.getSnoozeMinute() : alarm.getTimeMinute();
        int alarmSeconds = includeSnoozed ? alarm.getSnoozeSeconds() : 0;
>>>>>>> Stashed changes

        // First check if it's later today or later in the week
        for (int dayOfWeek = Calendar.SUNDAY; dayOfWeek <= Calendar.SATURDAY; ++dayOfWeek) {
            if (alarm.getRepeatingDay(dayOfWeek - 1) && dayOfWeek >= nowDay &&
<<<<<<< Updated upstream
                    !(dayOfWeek == nowDay && alarm.getTimeHour() < nowHour) &&
                    !(dayOfWeek == nowDay && alarm.getTimeHour() == nowHour && alarm.getTimeMinute() <= nowMinute)) {
=======
                    !(dayOfWeek == nowDay && alarmHour < nowHour) &&
                    !(dayOfWeek == nowDay && alarmHour == nowHour && alarmMinute < nowMinute) &&
                    !(dayOfWeek == nowDay && alarmHour == nowHour &&
                            alarmMinute == nowMinute && alarmSeconds <= nowSeconds)) {
>>>>>>> Stashed changes
                // Only increment the calendar if the alarm isn't for later today
                if (dayOfWeek > nowDay) {
                    calendarAlarm.add(Calendar.DATE, dayOfWeek - nowDay);
                }
                thisWeek = true;
                break;
            }
        }

        if (!thisWeek) {
            for (int dayOfWeek = Calendar.SUNDAY; dayOfWeek <= Calendar.SATURDAY; ++dayOfWeek) {
                if (alarm.getRepeatingDay(dayOfWeek - 1) && dayOfWeek <= nowDay) {
                    calendarAlarm.add(Calendar.DATE, (7 - nowDay) + dayOfWeek);
                    break;
                }
            }
        }

        return calendarAlarm.getTimeInMillis();
    }

    private static PendingIntent createPendingIntent(Context context, Alarm alarm) {
        Intent intent = new Intent(context, AlarmWakeReceiver.class);
        intent.putExtra(ALARM_ID, alarm.getId());

        return PendingIntent.getBroadcast(context, (int)Math.abs(alarm.getId().getLeastSignificantBits()), intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static void setAlarm(Context context, long time, PendingIntent pendingIntent) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, time, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);
        }
    }
}
