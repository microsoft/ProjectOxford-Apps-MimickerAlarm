package com.microsoft.mimicker.utilities;

import android.content.Context;
import android.view.Window;
import android.view.WindowManager;

import com.ibm.icu.text.SimpleDateFormat;
import com.microsoft.mimicker.R;

import java.text.Format;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

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
