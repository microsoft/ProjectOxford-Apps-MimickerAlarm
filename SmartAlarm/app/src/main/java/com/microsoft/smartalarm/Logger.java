package com.microsoft.smartalarm;

import android.app.Activity;
import android.provider.Settings.Secure;
import android.support.annotation.Nullable;

import com.microsoft.applicationinsights.contracts.SeverityLevel;
import com.microsoft.applicationinsights.library.ApplicationInsights;
import com.microsoft.applicationinsights.library.TelemetryClient;
import com.microsoft.applicationinsights.library.TelemetryContext;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Logger {
    public interface UserAction{
        String ALARM_SNOOZE = "Alarm Snoozed";
        String ALARM_DISMISS = "Alarm Dismissed";
        String GAME_COLOR = "Play: Game Color Finder";
        String GAME_COLOR_FAIL = "Fail: Game Color Finder";
        String GAME_COLOR_TIMEOUT = "Timeout: Game Color Finder";
        String GAME_COLOR_SUCCESS = "Success: Game Color Finder";
        String GAME_TWISTER = "Play: Game Tongue Twister";
        String GAME_TWISTER_FAIL = "Fail: Game Tongue Twister";
        String GAME_TWISTER_TIMEOUT = "Timeout: Game Tongue Twister";
        String GAME_TWISTER_SUCCESS = "Success: Game Tongue Twister";
    }

    public interface Duration{
        String OXFORD_VISION_ANALYZE = "OxfordVisionAnalyze";
    }

    private final static Boolean LOG_IN_DEBUG = false; // Use this to log even if in debug mode
    private static Boolean sStarted = false;
    private static HashMap<UUID, Long> sTimerMap;

    private static Boolean isLogging(){
        return !BuildConfig.DEBUG || LOG_IN_DEBUG;
    }

    public static void init(Activity caller){
        if (!sStarted && isLogging()) {
            ApplicationInsights.setup(caller.getApplicationContext(), caller.getApplication());
            String android_id = Secure.getString(caller.getContentResolver(), Secure.ANDROID_ID);
            ApplicationInsights.disableAutoCollection();
            TelemetryContext context = ApplicationInsights.getTelemetryContext();
            context.setAccountId(android_id);
            context.setAuthenticatedUserId(android_id);
            ApplicationInsights.start();
            sStarted = true;
            if (sTimerMap == null){
                sTimerMap = new HashMap<>();
            }
        }
    }

    public static void trackException(Exception ex){
        if (isLogging()) {
            try {
                TelemetryClient.getInstance().trackHandledException(ex);
            }
            catch (Exception appInsightEx) {
                trackException(appInsightEx);
            }
        }
    }

    public static void trackUserAction(String action, @Nullable Map<String, String> properties, @Nullable Map<String, Double> metrics){
        if (isLogging()) {
            try {
                TelemetryClient.getInstance().trackEvent(action, properties, metrics);
            }
            catch (Exception appInsightEx) {
                trackException(appInsightEx);
            }
        }
    }

    public static UUID trackDurationStart(){
        UUID timerIdentifier = UUID.randomUUID();
        sTimerMap.put(timerIdentifier, System.currentTimeMillis());
        return timerIdentifier;
    }
    public static void trackDurationEnd(UUID timerIdentifier, String action, @Nullable Map<String, String> properties){
        if (sTimerMap == null || !sTimerMap.containsKey(timerIdentifier)){
            return;
        }
        long startTime = sTimerMap.get(timerIdentifier);
        if (isLogging()) {
            // App Insights has a bug where if you pass in null properties, it just fails...
            // Even thought the SDK seems to allow it -_-
            if (properties == null){
                properties = new HashMap<>();
            }
            try {
                TelemetryClient.getInstance().trackMetric(action, System.currentTimeMillis() - startTime, properties);
            }
            catch (Exception appInsightEx) {
                trackException(appInsightEx);
            }
        }
        sTimerMap.remove(timerIdentifier);
    }

    public static void trackError(String errorMessage){
        if (isLogging()) {
            Map<String, String> properties = new HashMap<String, String>();
            properties.put("Severity", SeverityLevel.ERROR.toString());
            properties.put("Message", errorMessage);
            try {
                TelemetryClient.getInstance().trackTrace("Error", properties);
            }
            catch (Exception appInsightEx) {
                trackException(appInsightEx);
            }
        }
    }
}
