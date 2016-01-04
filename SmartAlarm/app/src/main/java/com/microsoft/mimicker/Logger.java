package com.microsoft.mimicker;

import android.content.Context;
import android.provider.Settings.Secure;
import android.util.Log;

import com.mixpanel.android.mpmetrics.MixpanelAPI;

import java.util.Collections;

public class Logger {

    private final static Boolean LOG_IN_DEBUG = false; // Use this to log even if in debug mode
    private final static String TAG = "Logger";
    private static Boolean sStarted = false;
    private static Context sContext;
    private static String sMixpanelToken;
    // These are only used to time event in debug mode
    private static long debugTimerStart;
    private static String debugTimerName = null;

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

    public static void local(String s) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, s);
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
        else {
            if (debugTimerName != null) {
                long duration = System.currentTimeMillis() - debugTimerStart;
                Log.d(TAG, debugTimerName + " took " + duration + " milliseconds");
                debugTimerName = null;
            }
            debugPrint(loggable);
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
        else {
            debugTimerName = loggable.Name;
            debugTimerStart = System.currentTimeMillis();
            debugPrint(loggable);
        }
    }

    public static void trackException(Exception ex) {
        if (isLogging()) {
            try {
                Loggable.AppException appException = new Loggable.AppException(Loggable.Key.APP_EXCEPTION, ex);
                MixpanelAPI.getInstance(sContext, sMixpanelToken).track(appException.Name, appException.Properties);
            } catch (Exception mixpanelEx) {
                Log.e(TAG, mixpanelEx.getMessage());
            }
        }
        else {
            Log.e(TAG, ex.getMessage());
        }
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

    public static void debugPrint(Loggable loggable) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, loggable.Name);
            Log.d(TAG, loggable.Properties.toString());
        }
    }
}
