package com.microsoft.mimicker.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.microsoft.mimicker.database.AlarmCursorWrapper;
import com.microsoft.mimicker.database.AlarmDatabaseHelper;
import com.microsoft.mimicker.database.AlarmDbSchema.AlarmTable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AlarmList {
    private static AlarmList sAlarmList;

    private Context mContext;
    private SQLiteDatabase mDatabase;

    private AlarmList(Context context) {
        mContext = context.getApplicationContext();
        mDatabase = new AlarmDatabaseHelper(mContext)
                .getWritableDatabase();
    }

    public static AlarmList get(Context context) {
        if (sAlarmList == null) {
            sAlarmList = new AlarmList(context);
        }
        return sAlarmList;
    }

    private static ContentValues populateContentValues(Alarm alarm) {
        ContentValues values = new ContentValues();
        values.put(AlarmTable.Columns.UUID, alarm.getId().toString());
        values.put(AlarmTable.Columns.TITLE, alarm.getTitle());
        values.put(AlarmTable.Columns.ENABLED, alarm.isEnabled() ? 1 : 0);
        values.put(AlarmTable.Columns.HOUR, alarm.getTimeHour());
        values.put(AlarmTable.Columns.MINUTE, alarm.getTimeMinute());
        values.put(AlarmTable.Columns.TONE, alarm.getAlarmTone() != null ? alarm.getAlarmTone().toString() : "");

        String repeatingDays = "";
        for (int i = 0; i < 7; ++i) {
            repeatingDays += alarm.getRepeatingDay(i) + ",";
        }
        values.put(AlarmTable.Columns.DAYS, repeatingDays);
        values.put(AlarmTable.Columns.VIBRATE, alarm.shouldVibrate());
        values.put(AlarmTable.Columns.TONGUE_TWISTER, alarm.isTongueTwisterEnabled());
        values.put(AlarmTable.Columns.COLOR_CAPTURE, alarm.isColorCaptureEnabled());
        values.put(AlarmTable.Columns.EXPRESS_YOURSELF, alarm.isExpressYourselfEnabled());
        values.put(AlarmTable.Columns.NEW, alarm.isNew() ? 1 : 0);

        return values;
    }

    public void addAlarm(Alarm alarm) {
        ContentValues values = populateContentValues(alarm);

        mDatabase.insert(AlarmTable.NAME, null, values);
    }

    public List<Alarm> getAlarms() {
        List<Alarm> alarms = new ArrayList<>();

        AlarmCursorWrapper cursor = queryAlarms(null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            alarms.add(cursor.getAlarm());
            cursor.moveToNext();
        }
        cursor.close();

        return alarms;
    }

    public Alarm getAlarm(UUID id) {
        AlarmCursorWrapper cursor = queryAlarms(
                AlarmTable.Columns.UUID + " = ?",
                new String[]{id.toString()}
        );

        try {
            if (cursor.getCount() == 0) {
                return null;
            }

            cursor.moveToFirst();
            return cursor.getAlarm();
        } finally {
            cursor.close();
        }
    }

    public void updateAlarm(Alarm alarm) {
        ContentValues values = populateContentValues(alarm);

        mDatabase.update(AlarmTable.NAME, values,
                AlarmTable.Columns.UUID + " = ?",
                new String[]{alarm.getId().toString()});
    }

    public void deleteAlarm(Alarm alarm) {
        mDatabase.delete(AlarmTable.NAME,
                AlarmTable.Columns.UUID + " = ?",
                new String[] { alarm.getId().toString() });
    }

    private AlarmCursorWrapper queryAlarms(String queryClause, String[] queryArgs) {
        Cursor cursor = mDatabase.query(
                AlarmTable.NAME,
                null, // gets all columns
                queryClause,
                queryArgs,
                null,
                null,
                null
        );

        return new AlarmCursorWrapper(cursor);
    }
}
