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

package com.microsoft.mimickeralarm.utilities;

import android.content.Context;
import android.provider.Settings.Secure;
import android.util.Log;

import com.microsoft.mimickeralarm.BuildConfig;
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

                sMixpanelToken = KeyUtilities.getToken(caller, "mixpanel");

                if (sMixpanelToken == null || sMixpanelToken.equals("")){
                    sStarted = false;
                    return;
                }

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
        if (isLogging() && sStarted) {
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
        if (isLogging() && sStarted) {
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
        if (isLogging() && sStarted) {
            try {
                Loggable.AppException appException = new Loggable.AppException(Loggable.Key.APP_EXCEPTION, ex);
                MixpanelAPI.getInstance(sContext, sMixpanelToken).track(appException.Name, appException.Properties);
            } catch (Exception mixpanelEx) {
                Log.e(TAG, mixpanelEx.getMessage());
            }
        }
        else {
            // This is called in a debug only scenario
            Log.e(TAG, "Logging exception:" , ex);
        }
    }

    public static void flush() {
        if (isLogging() && sStarted) {
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
