package com.microsoft.mimicker.appcore;

import android.app.Application;
import android.content.Context;

public class AlarmApplication extends Application {
    private static Context sContext;

    public static Context getAppContext() {
        return AlarmApplication.sContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        AlarmApplication.sContext = getApplicationContext();
    }
}
