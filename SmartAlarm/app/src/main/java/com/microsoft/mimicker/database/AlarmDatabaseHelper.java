package com.microsoft.mimicker.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.microsoft.mimicker.database.AlarmDbSchema.AlarmTable;

/**
 * This class implements the methods needed to create the SQLite alarm database.
 *
 * onCreate - is called to create a new database with a SQL query
 * onUpdate - is called in the cases where the database is already created.  Depending on the
 * database version, new columns are added.
 *
 */
public class AlarmDatabaseHelper extends SQLiteOpenHelper{

    private static final int DATABASE_VERSION = 5;
    private static final String DATABASE_NAME = "alarmDatabase.db";

    public AlarmDatabaseHelper(Context context) { super(context, DATABASE_NAME, null, DATABASE_VERSION); }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL( "create table " + AlarmTable.NAME + "(" +
                " _id integer primary key autoincrement, " +
                AlarmTable.Columns.UUID + ", " +
                AlarmTable.Columns.TITLE + ", " +
                AlarmTable.Columns.ENABLED + ", " +
                AlarmTable.Columns.HOUR + ", " +
                AlarmTable.Columns.MINUTE + ", " +
                AlarmTable.Columns.DAYS + ", " +
                AlarmTable.Columns.TONE + ", " +
                AlarmTable.Columns.VIBRATE + ", " +
                AlarmTable.Columns.TONGUE_TWISTER + ", " +
                AlarmTable.Columns.COLOR_CAPTURE + ", " +
                AlarmTable.Columns.EXPRESS_YOURSELF + ", " +
                AlarmTable.Columns.NEW + ", " +
                AlarmTable.Columns.SNOOZED + ", " +
                AlarmTable.Columns.SNOOZED_HOUR + ", " +
                AlarmTable.Columns.SNOOZED_MINUTE + ", " +
                AlarmTable.Columns.SNOOZED_SECONDS +
                ")"
        );
    }

    // TODO: Clean this up before publish
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch (oldVersion) {
            case 1:
                db.execSQL("ALTER TABLE " + AlarmTable.NAME + " ADD COLUMN " + AlarmTable.Columns.VIBRATE);
                db.execSQL("ALTER TABLE " + AlarmTable.NAME + " ADD COLUMN " + AlarmTable.Columns.TONGUE_TWISTER);
                db.execSQL("ALTER TABLE " + AlarmTable.NAME + " ADD COLUMN " + AlarmTable.Columns.COLOR_CAPTURE);
                db.execSQL("ALTER TABLE " + AlarmTable.NAME + " ADD COLUMN " + AlarmTable.Columns.EXPRESS_YOURSELF);
                db.execSQL("ALTER TABLE " + AlarmTable.NAME + " ADD COLUMN " + AlarmTable.Columns.NEW);
                db.execSQL("ALTER TABLE " + AlarmTable.NAME + " ADD COLUMN " + AlarmTable.Columns.SNOOZED);
                db.execSQL("ALTER TABLE " + AlarmTable.NAME + " ADD COLUMN " + AlarmTable.Columns.SNOOZED_HOUR);
                db.execSQL("ALTER TABLE " + AlarmTable.NAME + " ADD COLUMN " + AlarmTable.Columns.SNOOZED_MINUTE);
                db.execSQL("ALTER TABLE " + AlarmTable.NAME + " ADD COLUMN " + AlarmTable.Columns.SNOOZED_SECONDS);
                break;
            case 2:
                db.execSQL("ALTER TABLE " + AlarmTable.NAME + " ADD COLUMN " + AlarmTable.Columns.NEW);
                db.execSQL("ALTER TABLE " + AlarmTable.NAME + " ADD COLUMN " + AlarmTable.Columns.SNOOZED);
                db.execSQL("ALTER TABLE " + AlarmTable.NAME + " ADD COLUMN " + AlarmTable.Columns.SNOOZED_HOUR);
                db.execSQL("ALTER TABLE " + AlarmTable.NAME + " ADD COLUMN " + AlarmTable.Columns.SNOOZED_MINUTE);
                db.execSQL("ALTER TABLE " + AlarmTable.NAME + " ADD COLUMN " + AlarmTable.Columns.SNOOZED_SECONDS);
                break;
            case 3:
                db.execSQL("ALTER TABLE " + AlarmTable.NAME + " ADD COLUMN " + AlarmTable.Columns.SNOOZED);
                db.execSQL("ALTER TABLE " + AlarmTable.NAME + " ADD COLUMN " + AlarmTable.Columns.SNOOZED_HOUR);
                db.execSQL("ALTER TABLE " + AlarmTable.NAME + " ADD COLUMN " + AlarmTable.Columns.SNOOZED_MINUTE);
                db.execSQL("ALTER TABLE " + AlarmTable.NAME + " ADD COLUMN " + AlarmTable.Columns.SNOOZED_SECONDS);
                break;
            case 4:
                db.execSQL("ALTER TABLE " + AlarmTable.NAME + " ADD COLUMN " + AlarmTable.Columns.SNOOZED_SECONDS);
        }
    }
}
