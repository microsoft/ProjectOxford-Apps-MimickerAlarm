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

package com.microsoft.mimickeralarm.ringing;

import android.content.Intent;

import java.util.LinkedList;
import java.util.Queue;

/**
 * This class is an abstract utility class that implements a serialized queue dispatcher.  The
 * caller will register alarm intents using registerAlarm and the subsequent alarms will be
 * dispatched on each call of alarmRingingSessionCompleted.
 */
public abstract class AlarmRingingSessionDispatcher {
    Queue<Intent> mAlarmIntentQueue;

    public AlarmRingingSessionDispatcher() {
        mAlarmIntentQueue = new LinkedList<>();
    }

    public abstract void beforeDispatchFirstAlarmRingingSession();

    public abstract void dispatchAlarmRingingSession(Intent intent);

    public abstract void allAlarmRingingSessionsComplete();

    protected void registerAlarm(Intent intent) {
        // We add the new intent to the queue. If successful, we check the queue length
        // and if this is the only work item, we dispatch.
        // We use 'offer' here rather than 'add' as it is non-throwing
        if (mAlarmIntentQueue.offer(intent) &&
                mAlarmIntentQueue.size() == 1) {
            beforeDispatchFirstAlarmRingingSession();
            dispatchAlarmRingingSession(mAlarmIntentQueue.peek());
        }
    }

    protected void alarmRingingSessionCompleted() {
        // On completion of work, we take the head item off the queue and dispatch the
        // next work item if necessary. We use 'poll' rather than 'remove' as it is non-throwing.
        if (mAlarmIntentQueue.poll() != null) {
            dispatchAlarmRingingSession(mAlarmIntentQueue.peek());
        }
        if (mAlarmIntentQueue.isEmpty()) {
            allAlarmRingingSessionsComplete();
        }
    }
}
