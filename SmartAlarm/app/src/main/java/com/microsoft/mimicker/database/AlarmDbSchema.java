package com.microsoft.mimicker.database;

public class AlarmDbSchema {
    public static final class AlarmTable {
        public static final String NAME = "alarms";

        public static final class Columns {
            public static final String UUID = "uuid";
            public static final String TITLE = "title";
            public static final String HOUR = "hour";
            public static final String MINUTE = "minute";
            public static final String DAYS = "days";
            public static final String TONE = "tone";
            public static final String ENABLED = "enabled";
            public static final String VIBRATE = "vibrate";
            public static final String TONGUE_TWISTER = "tongue_twister";
            public static final String COLOR_CAPTURE = "color_collector";  // TODO: Clean this up before publish
            public static final String EXPRESS_YOURSELF = "express_yourself";
            public static final String NEW = "new";
            public static final String SNOOZED = "snoozed";
            public static final String SNOOZED_HOUR = "snoozed_hour";
            public static final String SNOOZED_MINUTE = "snoozed_minute";
            public static final String SNOOZED_SECONDS = "snoozed_seconds";
        }
    }
}
