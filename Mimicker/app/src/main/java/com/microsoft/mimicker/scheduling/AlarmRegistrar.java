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

package com.microsoft.mimicker.scheduling;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * This BroadcastReceiver is registered to be called for the following system intents:
 *
 * Boot - when the system is booted we want to re-register the alarms with the AlarmManager
 * Time/Date/Timezone change - We re-register with the AlarmManager when any of these changes happen
 * in the system.
 *
 * Note that this receiver is called in the main thread, so there should not be any thread safety
 * issues with interfering with the alarm user interface functionality.
 *
 * Once we have re-registered alarms, we check to see if we need to display a notification by
 * calling into the AlarmNotificationManager.
 */
public class AlarmRegistrar extends BroadcastReceiver {

    private static void refreshAlarms(Context context) {
        AlarmScheduler.cancelAlarms(context);
        if (AlarmScheduler.scheduleAlarms(context)) {
            AlarmNotificationManager.get(context).handleNextAlarmNotificationStatus();
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        refreshAlarms(context);
    }
}
