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

    private static Boolean sStarted = false;

    public static void init(Activity caller){
        if (!sStarted) {
            ApplicationInsights.setup(caller.getApplicationContext(), caller.getApplication());
            String android_id = Secure.getString(caller.getContentResolver(), Secure.ANDROID_ID);
            ApplicationInsights.disableAutoCollection();
            TelemetryContext context = ApplicationInsights.getTelemetryContext();
            context.setAccountId(android_id);
            context.setAuthenticatedUserId(android_id);
            ApplicationInsights.start();
            sStarted = true;
        }
    }

    public static void trackException(Exception ex){
        TelemetryClient.getInstance().trackHandledException(ex);
    }

    public static void trackUserAction(String action, @Nullable Map<String, String> properties, @Nullable Map<String, Double> metrics){
        TelemetryClient.getInstance().trackEvent(action, properties, metrics);
    }

    public static void trackError(String errorMessage){
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("Severity", SeverityLevel.ERROR.toString());
        properties.put("Message", errorMessage);
        TelemetryClient.getInstance().trackTrace("Error", properties);
    }
}
