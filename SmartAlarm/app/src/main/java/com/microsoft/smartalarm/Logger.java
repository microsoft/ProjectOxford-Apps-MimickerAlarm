package com.microsoft.smartalarm;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.util.Log;

import com.mixpanel.android.mpmetrics.MixpanelAPI;

import java.util.Collections;

public class Logger {

    private final static Boolean LOG_IN_DEBUG = false; // Use this to log even if in debug mode
    private static Boolean sStarted = false;
    private static Context sContext;
    private static String sMixpanelToken;
    private final static String TAG = "Logger";

    private static Boolean isLogging(){
        return !BuildConfig.DEBUG || LOG_IN_DEBUG;
    }

    public static void init(Context caller){
        sContext = caller;
        if (!sStarted && isLogging()) {
            String android_id = Secure.getString(caller.getContentResolver(), Secure.ANDROID_ID);
            try {
                sMixpanelToken = Util.getToken(caller, "mixpanel");
                sStarted = true;

                MixpanelAPI mixpanel = MixpanelAPI.getInstance(sContext, sMixpanelToken);
                mixpanel.identify(android_id);
                mixpanel.getPeople().identify(android_id);
                mixpanel.getPeople().set("name", android_id);
                mixpanel.getPeople().set("Build Version", BuildConfig.VERSION_NAME);
                mixpanel.getPeople().setMap(Collections.<String, Object>unmodifiableMap(mixpanel.getDeviceInfo()));
            }
            catch (Exception ex){
                trackException(ex);
            }
        }
    }

    public static void track(Loggable loggable){
        if (isLogging()) {
            try {
                MixpanelAPI.getInstance(sContext, sMixpanelToken).track(loggable.Name, loggable.Properties);
            }
            catch (Exception ex) {
                trackException(ex);
            }
        }
    }

    public static void trackDurationStart(Loggable loggable){
        if (isLogging()) {
            try {
                MixpanelAPI.getInstance(sContext, sMixpanelToken).timeEvent(loggable.Name);
            }
            catch (Exception ex) {
                trackException(ex);
            }
        }
    }

    public static void trackException(Exception ex) {
       //TODO : IMPLEMENT
        Log.e(TAG, ex.getMessage());
    }

    public static void flush() {
        if (isLogging()) {
            try {
                MixpanelAPI.getInstance(sContext, sMixpanelToken).flush();
            } catch (Exception ex) {
                trackException(ex);
            }
        }
    }
}
