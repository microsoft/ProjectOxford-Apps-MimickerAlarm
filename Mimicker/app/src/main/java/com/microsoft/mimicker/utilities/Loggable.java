/*
 *
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license.
 *
 * Project Oxford: http://ProjectOxford.ai
 *
 * Project Oxford Mimicker Alarm Github:
 * https://github.com/Microsoft/ProjectOxford-Apps-MimickerAlarm
 *
 * Copyright (c) Microsoft Corporation
 * All rights reserved.
 *
 * MIT License:
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED ""AS IS"", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package com.microsoft.mimicker.utilities;

import com.microsoft.projectoxford.emotion.contract.RecognizeResult;
import com.microsoft.projectoxford.vision.contract.AnalyzeResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Loggable {
    public String Name;
    JSONObject Properties;

    public void putProp(String property, Object value) {
        try {
            Properties.put(property, value);
        }
        catch (JSONException ex) {
            Logger.trackException(ex);
        }
    }

    public void putJSON(JSONObject json) {
        try {
            Iterator<?> keys = json.keys();

            while( keys.hasNext() ) {
                String key = (String)keys.next();
                Properties.put(key, json.get(key));
            }
        }
        catch (JSONException ex) {
            Logger.trackException(ex);
        }
    }

    public void putVision(AnalyzeResult result) {
        try {
            Properties.put("Color Dominants", result.color.dominantColors);
            Properties.put("Color Dominant FG", result.color.dominantColorForeground);
            Properties.put("Color Dominant BG", result.color.dominantColorBackground);
            Properties.put("Color Accent", result.color.accentColor);
        }
        catch (JSONException ex) {
            Logger.trackException(ex);
        }
    }

    public void putEmotions(List<RecognizeResult> results) {
        try {
            ArrayList<Double> scores = new ArrayList<>();

            for (int i = 0; i < results.size(); i++) {
                scores.add(results.get(i).scores.anger);
            }
            Properties.put("Emotion Anger", new JSONArray(scores));

            for (int i = 0; i < results.size(); i++) {
                scores.set(i, results.get(i).scores.contempt);
            }
            Properties.put("Emotion Contempt", new JSONArray(scores));

            for (int i = 0; i < results.size(); i++) {
                scores.set(i, results.get(i).scores.disgust);
            }
            Properties.put("Emotion Disgust", new JSONArray(scores));

            for (int i = 0; i < results.size(); i++) {
                scores.set(i, results.get(i).scores.fear);
            }
            Properties.put("Emotion Fear", new JSONArray(scores));

            for (int i = 0; i < results.size(); i++) {
                scores.set(i, results.get(i).scores.happiness);
            }
            Properties.put("Emotion Happiness", new JSONArray(scores));

            for (int i = 0; i < results.size(); i++) {
                scores.set(i, results.get(i).scores.neutral);
            }
            Properties.put("Emotion Neutral", new JSONArray(scores));

            for (int i = 0; i < results.size(); i++) {
                scores.set(i, results.get(i).scores.sadness);
            }
            Properties.put("Emotion Sadness", new JSONArray(scores));

            for (int i = 0; i < results.size(); i++) {
                scores.set(i, results.get(i).scores.surprise);
            }
            Properties.put("Emotion Surprise", new JSONArray(scores));
        }
        catch (JSONException ex) {
            Logger.trackException(ex);
        }
    }

    public interface Key {
        String APP_ALARM_RINGING = "An alarm rang";
        String APP_EXCEPTION = "Exception caught";
        String APP_ERROR = "Error occurred";
        String APP_API_VISION = "Calling Vision API";
        String APP_API_EMOTION = "Calling Emotion API";
        String APP_API_SPEECH = "Calling Speech API";

        String ACTION_ALARM_SNOOZE = "Snoozed an alarm";
        String ACTION_ALARM_DISMISS = "Dismissed an alarm";
        String ACTION_ALARM_EDIT = "Editing an alarm";
        String ACTION_ALARM_CREATE = "Creating a new alarm";
        String ACTION_ALARM_SAVE = "Saving changes to an alarm";
        String ACTION_ALARM_SAVE_DISCARD = "Discarding changes to an alarm";
        String ACTION_ALARM_DELETE = "Deleting an alarm";

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

        String ACTION_GAME_NONETWORK = "Played an no network game";
        String ACTION_GAME_NONETWORK_TIMEOUT = "Timed out on an no network game";
        String ACTION_GAME_NONETWORK_SUCCESS = "Finished an no network game";

        String ACTION_ONBOARDING = "Started onboarding";
        String ACTION_ONBOARDING_SKIP = "Skipped onboarding";
        String ACTION_ONBOARDING_TOS_ACCEPT = "Accepted ToS";
        String ACTION_ONBOARDING_TOS_DECLINE = "Declined ToS";

        String ACTION_LEARN_MORE = "Reading Learn More";

        String ACTION_SHARE = "Sharing";

        String PROP_QUESTION = "Question";
        String PROP_DIFF = "Difference";
    }

    public static class UserAction extends Loggable {
        public UserAction (String name) {
            Name = name;
            Properties = new JSONObject();
            try {
                Properties.put("Type", "User Action");
            }
            catch (JSONException jsonEx) {
            }
        }
    }

    public static class AppAction extends Loggable {
        public AppAction (String name) {
            Name = name;
            Properties = new JSONObject();
            try {
                Properties.put("Type", "App Action");
            }
            catch (JSONException jsonEx) {
            }
        }
    }

    public static class AppException extends Loggable {
        public AppException (String name, Exception ex) {
            Name = name;
            Properties = new JSONObject();
            try {
                Properties.put("Type", "Exception");
                Properties.put("Message", ex);
                Properties.put("Detailed Message", ex.getMessage());
            }
            catch (JSONException jsonEx) {
            }
        }
    }

    public static class AppError extends Loggable {
        public AppError (String name, String error) {
            Name = name;
            Properties = new JSONObject();
            try {
                Properties.put("Type", "Error");
                Properties.put("Message", error);
            }
            catch (JSONException jsonEx) {
            }
        }
    }
}
