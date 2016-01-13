package com.microsoft.mimicker.ringing;

import android.content.Intent;

import java.util.LinkedList;
import java.util.Queue;

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
