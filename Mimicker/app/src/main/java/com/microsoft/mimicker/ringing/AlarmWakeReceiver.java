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

package com.microsoft.mimicker.ringing;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

/**
 * This class is a special BroadcastReceiver that receives the PendingIntent from the AlarmManager
 * while holding the wakelock.  It forwards the intent to the AlarmRingingService to dispatch the
 * alarm. The service calls AlarmWakeReceiver.completeWakefulIntent() once it has acquired the
 * wakelock.
 */
public class AlarmWakeReceiver extends WakefulBroadcastReceiver {

    public final String TAG = this.getClass().getSimpleName();

    // We use a broadcast receiver with the PendingIntent for the AlarmManager, as this approach
    // is more reliable that using a service. See for details: http://hiqes.com/android-alarm-ins-outs/
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Broadcast from AlarmManager received!");
        Intent serviceIntent = new Intent(AlarmRingingService.ACTION_DISPATCH_ALARM);
        serviceIntent.setClass(context, AlarmRingingService.class);
        serviceIntent.putExtras(intent);
        startWakefulService(context, serviceIntent);
    }
}
