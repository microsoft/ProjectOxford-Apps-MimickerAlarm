package com.microsoft.smartalarm;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings.Secure;

import com.mixpanel.android.mpmetrics.MixpanelAPI;

import java.util.Collections;

public class Logger {

    private final static Boolean LOG_IN_DEBUG = true; // Use this to log even if in debug mode
    private static Boolean sStarted = false;
    private static Context sContext;
    private static String sMixpanelToken;

    private static Boolean isLogging(){
        return !BuildConfig.DEBUG || LOG_IN_DEBUG;
    }

    public static void init(Activity caller){
        sContext = caller;
        if (!sStarted && isLogging()) {
            String android_id = Secure.getString(caller.getContentResolver(), Secure.ANDROID_ID);
            sStarted = true;

            try {
                ApplicationInfo ai = caller.getPackageManager().getApplicationInfo(caller.getPackageName(), PackageManager.GET_META_DATA);
                Bundle bundle = ai.metaData;
                sMixpanelToken = bundle.getString("com.mixpanel.token");
            }
            catch (Exception ex){
            }

            MixpanelAPI mixpanel = MixpanelAPI.getInstance(sContext, sMixpanelToken);
            mixpanel.identify(android_id);
            mixpanel.getPeople().identify(android_id);
            mixpanel.getPeople().set("name", android_id);
            mixpanel.getPeople().set("Build Version", BuildConfig.VERSION_NAME);
            mixpanel.getPeople().setMap(Collections.<String, Object>unmodifiableMap(mixpanel.getDeviceInfo()));
        }
    }

    public static void track(Loggable loggable){
        if (isLogging()) {
            try {
                MixpanelAPI.getInstance(sContext, sMixpanelToken).track(loggable.Name, loggable.Properties);
            }
            catch (Exception appInsightEx) {
            }
        }
    }

    public static void trackDurationStart(Loggable loggable){
        MixpanelAPI.getInstance(sContext, sMixpanelToken).timeEvent(loggable.Name);
    }

    public static void trackException(Exception ex) {
       //TODO : IMPLEMENT
    }
}
