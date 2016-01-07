package com.microsoft.mimicker.ringing;

import android.content.Context;
import android.content.Intent;

import com.microsoft.mimicker.scheduling.AlarmNotificationManager;
import com.microsoft.mimicker.utilities.SharedWakeLock;

import java.util.LinkedList;
import java.util.Queue;

public final class AlarmRingingDispatcher {
    Queue<Intent> mAlarmIntentQueue;
    Context mContext;

    public AlarmRingingDispatcher(Context context) {
        mAlarmIntentQueue = new LinkedList<>();
        mContext = context;
    }

    public void register(Intent intent) {
        // We add the new intent to the queue. If successful, we check the queue length
        // and if this is the only work item, we then acquire the wakelock and dispatch.
        // We use 'offer' here rather than 'add' as it is non-throwing
        if (mAlarmIntentQueue.offer(intent) &&
                mAlarmIntentQueue.size() == 1) {
            SharedWakeLock.get(mContext).acquireFullWakeLock();
            dispatch(mAlarmIntentQueue.peek());
        }
    }

    private void dispatch(Intent intent) {
        if (intent != null) {
            Intent alarmIntent = new Intent(mContext, AlarmRingingActivity.class);
            alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            alarmIntent.putExtras(intent.getExtras());
            mContext.startActivity(alarmIntent);
        }
    }

    public void workCompleted() {
        // On completion of work, we take the head item off the queue and dispatch the
        // next work item if necessary. We use 'poll' rather than 'remove' as it is non-throwing.
        if (mAlarmIntentQueue.poll() != null) {
            dispatch(mAlarmIntentQueue.peek());
        }
        if (mAlarmIntentQueue.isEmpty()) {
            SharedWakeLock.get(mContext).releaseFullWakeLock();
            AlarmNotificationManager.get(mContext).handleAlarmNotificationStatus();
        }
    }
}
