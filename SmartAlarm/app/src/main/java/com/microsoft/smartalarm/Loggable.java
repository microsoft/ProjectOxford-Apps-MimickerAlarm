package com.microsoft.smartalarm;

import org.json.JSONException;
import org.json.JSONObject;

public class Loggable {
    String Name;
    String Type;
    JSONObject Properties;

    public void putProp(String property, Object value) {
        try {
            Properties.put(property, value);
        }
        catch (JSONException ex) {

        }
    }

    public static class UserAction extends Loggable {
        public UserAction (String name) {
            Name = name;
            Type = "User Action";
            Properties = new JSONObject();
        }
    }

    public interface Key {
        String ACTION_ALARM_SNOOZE = "Snoozed an alarm";
        String ACTION_ALARM_DISMISS = "Dismissed an alarm";

        String ACTION_GAME_COLOR = "Played a color finder game";
        String ACTION_GAME_COLOR_FAIL = "Failed a color finder game";
        String ACTION_GAME_COLOR_TIMEOUT = "Timed out on a color finder game";
        String ACTION_GAME_COLOR_SUCCESS = "Finished a color finder game";

        String ACTION_GAME_TWISTER = "Played a tongue twister game";
        String ACTION_GAME_TWISTER_FAIL = "Failed a tongue twister game";
        String ACTION_GAME_TWISTER_TIMEOUT = "Timed out on a tongue twister game";
        String ACTION_GAME_TWISTER_SUCCESS = "Finished a tongue twister game";

        String ACTION_GAME_EMOTION = "Played an emotion game";
        String ACTION_GAME_EMOTION_FAIL = "Failed an emotion game";
        String ACTION_GAME_EMOTION_TIMEOUT = "Timed out on an emotion game";
        String ACTION_GAME_EMOTION_SUCCESS = "Finished an emotion game";

        String ACTION_ONBOARDING = "Started onboarding";
        String ACTION_ONBOARDING_SKIP = "Skipped onboarding";

        String PROP_QUESTION = "Question";
        String PROP_DIFF = "Difference";
    }
}
