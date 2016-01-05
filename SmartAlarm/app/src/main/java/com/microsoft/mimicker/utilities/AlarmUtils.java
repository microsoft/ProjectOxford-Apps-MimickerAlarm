package com.microsoft.mimicker.utilities;

import android.content.Context;
import android.view.Window;
import android.view.WindowManager;

import com.ibm.icu.text.SimpleDateFormat;
import com.ibm.icu.util.Calendar;
import com.ibm.icu.text.MessageFormat;
import com.microsoft.mimicker.R;


import java.text.Format;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class AlarmUtils {

    // As per http://icu-project.org/apiref/icu4j/com/ibm/icu/text/SimpleDateFormat.html, we
    // need the format 'EEEEEE' to get a short weekday name
    private final static String TWO_CHARACTER_SHORT_DAY_PATTERN = "EEEEEE";
    private AlarmUtils() {}

    public static String getUserTimeString(Context context, int hour, int minute) {
        Format formatter = android.text.format.DateFormat.getTimeFormat(context);
        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);

        return formatter.format(calendar.getTime());
    }

    public static String getFullDateStringForNow() {
        Format formatter = java.text.DateFormat.getDateInstance(java.text.DateFormat.FULL);
        return formatter.format(Calendar.getInstance().getTime());
    }

    public static String[] getShortDayNames() {
        String[] dayNames = new String[7];
        Format formatter = new SimpleDateFormat(TWO_CHARACTER_SHORT_DAY_PATTERN, Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        for(int d = Calendar.SUNDAY, i = 0; d <= Calendar.SATURDAY; d++, i++) {
            calendar.set(Calendar.DAY_OF_WEEK, d);
            dayNames[i] = formatter.format(calendar.getTime()).toUpperCase(Locale.getDefault());
        }
        return dayNames;
    }

    public static String getShortDayNamesString(int[] daysOfWeek) {
        String dayNames = null;
        Format formatter = new SimpleDateFormat(TWO_CHARACTER_SHORT_DAY_PATTERN, Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        for(int day = 0; day < daysOfWeek.length; day++) {
            calendar.set(Calendar.DAY_OF_WEEK, daysOfWeek[day]);
            if (day == 0) {
                dayNames = formatter.format(calendar.getTime()).toUpperCase(Locale.getDefault());
            } else {
                dayNames += " " + formatter.format(calendar.getTime()).toUpperCase(Locale.getDefault());
            }
        }
        return dayNames;
    }

    public static String getDayPeriodSummaryString(Context context, int[] daysOfWeek) {
        int[] weekdays = { Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY };
        int[] weekend = { Calendar.SUNDAY, Calendar.SATURDAY };
        int[] everyday = { Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY };
        if (Arrays.equals(daysOfWeek, weekend)) {
            return context.getString(R.string.alarm_list_weekend);
        } else if (Arrays.equals(daysOfWeek, weekdays)) {
            return context.getString(R.string.alarm_list_weekdays);
        } else if (Arrays.equals(daysOfWeek, everyday)) {
            return context.getString(R.string.alarm_list_every_day);
        } else {
            return getShortDayNamesString(daysOfWeek);
        }
    }

    public static String getTimeUntilAlarmDisplayString(Context context, long timeUntilAlarm) {
        Calendar calendarNow = Calendar.getInstance();
        Calendar calendarAlarm = Calendar.getInstance();
        calendarAlarm.setTimeInMillis(timeUntilAlarm);
        Date alarmTime = calendarAlarm.getTime();

        // It's very important we make the fieldDifference calls in this order.  Each time
        // calendarNow moves closer to alarmTime by the difference units it returns. This implies
        // that you start with the largest calendar unit and move to smaller ones if you want
        // accurate results for different units between the two times.
        int days = Math.max(0, calendarNow.fieldDifference(alarmTime, Calendar.DATE));
        int hours = Math.max(0, calendarNow.fieldDifference(alarmTime, Calendar.HOUR_OF_DAY));
        int minutes = Math.max(0, calendarNow.fieldDifference(alarmTime, Calendar.MINUTE));

        Map<String,Integer> args = new HashMap<>();
        try {
            args.put("days", days);
            args.put("hours", hours);
            args.put("minutes", minutes);
        } catch (Exception e) {
            Logger.trackException(e);
        }

        int resourceIdForDisplayString;
        if (days > 0) {
            if (hours > 0 && minutes > 0) {
                resourceIdForDisplayString = R.string.alarm_set_day_hour_minute;
            } else if (hours > 0) {
                resourceIdForDisplayString = R.string.alarm_set_day_hour;
            } else if (minutes > 0) {
                resourceIdForDisplayString = R.string.alarm_set_day_minute;
            } else {
                resourceIdForDisplayString = R.string.alarm_set_day;
            }
        } else if (hours > 0) {
            if (minutes > 0) {
                resourceIdForDisplayString = R.string.alarm_set_hour_minute;
            } else {
                resourceIdForDisplayString = R.string.alarm_set_hour;
            }
        } else if (minutes > 0) {
            resourceIdForDisplayString = R.string.alarm_set_minute;
        } else {
            resourceIdForDisplayString = R.string.alarm_set_less_than_minute;
        }
        return new MessageFormat(context.getString(resourceIdForDisplayString)).format(args);
    }

    public static void setLockScreenFlags(Window window) {
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
    }

    public static void clearLockScreenFlags(Window window) {
        window.clearFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        window.clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        window.clearFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
    }
}
