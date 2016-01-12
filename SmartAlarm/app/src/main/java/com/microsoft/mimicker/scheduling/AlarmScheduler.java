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

    public static final String ARGS_ALARM_ID = "alarm_id";

    private AlarmScheduler() {
    }

    public static boolean scheduleAlarms(Context context) {
        List<Alarm> alarms =  AlarmList.get(context).getAlarms();
        boolean alarmsScheduled = false;
        for (Alarm alarm : alarms) {
            if (alarm.isEnabled()) {
                scheduleAlarm(context, alarm);
                alarmsScheduled = true;
            }
        }
        return alarmsScheduled;
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
        long time = getAlarmTime(calenderNow, alarm);
        setAlarm(context, time, pendingIntent);
        return time;
    }

    public static long getAlarmTime(Calendar calendarFrom, Alarm alarm) {
        if (alarm.isOneShot()) {
            return getOneShotAlarmTime(calendarFrom, alarm);
        } else {
            return getRepeatingAlarmTime(calendarFrom, alarm);
        }
    }

    private static long getOneShotAlarmTime(Calendar calendarFrom, Alarm alarm) {
        Calendar calendarAlarm = Calendar.getInstance();
        calendarAlarm.set(Calendar.HOUR_OF_DAY, alarm.getTimeHour());
        calendarAlarm.set(Calendar.MINUTE, alarm.getTimeMinute());
        calendarAlarm.set(Calendar.SECOND, 0);
        calendarAlarm.set(Calendar.MILLISECOND, 0);

        final int nowHour = calendarFrom.get(Calendar.HOUR_OF_DAY);
        final int nowMinute = calendarFrom.get(Calendar.MINUTE);

        // if we cannot schedule today then set the alarm for tomorrow
        if ((alarm.getTimeHour() < nowHour) ||
                (alarm.getTimeHour() == nowHour && alarm.getTimeMinute() <= nowMinute)) {
            calendarAlarm.add(Calendar.DATE, 1);
        }

        return calendarAlarm.getTimeInMillis();
    }

    private static long getRepeatingAlarmTime(Calendar calendarFrom, Alarm alarm) {
        Calendar calendarAlarm = Calendar.getInstance();
        calendarAlarm.set(Calendar.HOUR_OF_DAY, alarm.getTimeHour());
        calendarAlarm.set(Calendar.MINUTE, alarm.getTimeMinute());
        calendarAlarm.set(Calendar.SECOND, 0);
        calendarAlarm.set(Calendar.MILLISECOND, 0);
        boolean thisWeek = false;

        final int nowDay = calendarFrom.get(Calendar.DAY_OF_WEEK);
        final int nowHour = calendarFrom.get(Calendar.HOUR_OF_DAY);
        final int nowMinute = calendarFrom.get(Calendar.MINUTE);

        // First check if it's later today or later in the week
        for (int dayOfWeek = Calendar.SUNDAY; dayOfWeek <= Calendar.SATURDAY; ++dayOfWeek) {
            if (alarm.getRepeatingDay(dayOfWeek - 1) && dayOfWeek >= nowDay &&
                    !(dayOfWeek == nowDay && alarm.getTimeHour() < nowHour) &&
                    !(dayOfWeek == nowDay && alarm.getTimeHour() == nowHour &&
                            alarm.getTimeMinute() <= nowMinute)) {
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

    public static long getAlarmTimeIncludeSnoozed(Calendar calendarFrom, Alarm alarm) {
        if (alarm.isSnoozed()) {
            if (alarm.isOneShot()) {
                return getOneShotAlarmTimeSnoozed(calendarFrom, alarm);
            } else {
                return getRepeatingAlarmTimeSnoozed(calendarFrom, alarm);
            }
        } else {
            return getAlarmTime(calendarFrom, alarm);
        }
    }

    private static long getOneShotAlarmTimeSnoozed(Calendar calendarFrom, Alarm alarm) {
        Calendar calendarAlarm = Calendar.getInstance();
        calendarAlarm.set(Calendar.HOUR_OF_DAY, alarm.getSnoozeHour());
        calendarAlarm.set(Calendar.MINUTE, alarm.getSnoozeMinute());
        calendarAlarm.set(Calendar.SECOND, alarm.getSnoozeSeconds());
        calendarAlarm.set(Calendar.MILLISECOND, 0);

        final int nowHour = calendarFrom.get(Calendar.HOUR_OF_DAY);
        final int nowMinute = calendarFrom.get(Calendar.MINUTE);
        final int nowSeconds = calendarFrom.get(Calendar.SECOND);

        // if we cannot schedule today then set the alarm for tomorrow
        if ((alarm.getSnoozeHour() < nowHour) ||
            (alarm.getSnoozeHour() == nowHour && alarm.getSnoozeMinute() < nowMinute) ||
            (alarm.getSnoozeHour() == nowHour && alarm.getSnoozeMinute() == nowMinute &&
                    alarm.getSnoozeSeconds() <= nowSeconds)) {
            calendarAlarm.add(Calendar.DATE, 1);
        }

        return calendarAlarm.getTimeInMillis();
    }

    private static long getRepeatingAlarmTimeSnoozed(Calendar calendarFrom, Alarm alarm) {
        Calendar calendarAlarm = Calendar.getInstance();
        calendarAlarm.set(Calendar.HOUR_OF_DAY, alarm.getSnoozeHour());
        calendarAlarm.set(Calendar.MINUTE, alarm.getSnoozeMinute());
        calendarAlarm.set(Calendar.SECOND, alarm.getSnoozeSeconds());
        calendarAlarm.set(Calendar.MILLISECOND, 0);

        boolean thisWeek = false;
        final int nowDay = calendarFrom.get(Calendar.DAY_OF_WEEK);
        final int nowHour = calendarFrom.get(Calendar.HOUR_OF_DAY);
        final int nowMinute = calendarFrom.get(Calendar.MINUTE);
        final int nowSeconds = calendarFrom.get(Calendar.SECOND);

        // First check if it's later today or later in the week
        for (int dayOfWeek = Calendar.SUNDAY; dayOfWeek <= Calendar.SATURDAY; ++dayOfWeek) {
            if (alarm.getRepeatingDay(dayOfWeek - 1) && dayOfWeek >= nowDay &&
                    !(dayOfWeek == nowDay && alarm.getSnoozeHour() < nowHour) &&
                    !(dayOfWeek == nowDay && alarm.getSnoozeHour() == nowHour &&
                            alarm.getSnoozeMinute() < nowMinute) &&
                    !(dayOfWeek == nowDay && alarm.getSnoozeHour() == nowHour &&
                            alarm.getSnoozeMinute() == nowMinute &&
                            alarm.getSnoozeSeconds() <= nowSeconds)) {
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

    public static long snoozeAlarm(Context context, Alarm alarm, int snoozePeriod) {
        PendingIntent pendingIntent = createPendingIntent(context, alarm);
        Calendar calendarAlarm = Calendar.getInstance();
        long now = calendarAlarm.getTimeInMillis();
        calendarAlarm.setTimeInMillis(now + snoozePeriod);
        long snoozeTime = calendarAlarm.getTimeInMillis();
        setAlarm(context, snoozeTime, pendingIntent);
        return snoozeTime;
    }

    public static void cancelAlarm(Context context, Alarm alarm) {
        PendingIntent pIntent = createPendingIntent(context, alarm);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pIntent);
    }

    private static PendingIntent createPendingIntent(Context context, Alarm alarm) {
        Intent intent = new Intent(context, AlarmWakeReceiver.class);
        intent.putExtra(ARGS_ALARM_ID, alarm.getId());

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
