package com.microsoft.smartalarm;

import android.content.Context;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.Format;
import java.util.Calendar;
import java.util.Locale;

public class AlarmUtils {
    public static String getShortTimeString(int hour, int minute) {
        Format formatter = DateFormat.getTimeInstance(DateFormat.SHORT);
        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);

        return formatter.format(calendar.getTime());
    }

    public static String[] getShortDayNames() {
        String[] dayNames = new String[7];
        DateFormatSymbols dateFormatSymbols = new DateFormatSymbols();
        for(int d = Calendar.SUNDAY, i = 0; d <= Calendar.SATURDAY; d++, i++) {
            String dayName = dateFormatSymbols.getShortWeekdays()[d].toUpperCase(Locale.getDefault());
            if (dayName.length() == 3) {
                dayName = dayName.substring(0, 2);
            }
            dayNames[i] = dayName;
        }
        return dayNames;
    }

    public static int convertDpToPixels(Context context, int dp) {
        return (int) ((dp * context.getResources().getDisplayMetrics().density) + 0.5);
    }
}
