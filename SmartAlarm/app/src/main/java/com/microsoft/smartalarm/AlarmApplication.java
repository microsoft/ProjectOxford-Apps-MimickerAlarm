package com.microsoft.smartalarm;

import android.app.Application;
import android.content.Context;

public class AlarmApplication extends Application {
    private static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();
        AlarmApplication.sContext = getApplicationContext();
    }

    public static Context getAppContext() {
        return AlarmApplication.sContext;
    }
}
