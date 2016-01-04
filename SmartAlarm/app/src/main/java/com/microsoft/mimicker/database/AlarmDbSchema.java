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
            public static final String TONGUETWISTER = "tongue_twister";
            public static final String COLORCOLLECTOR = "color_collector";
            public static final String EXPRESSYOURSELF = "express_yourself";
            public static final String NEW = "new";
        }
    }
}
