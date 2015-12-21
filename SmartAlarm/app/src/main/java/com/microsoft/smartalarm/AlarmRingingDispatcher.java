package com.microsoft.smartalarm;

import android.content.Context;
import android.content.Intent;

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
        if (mAlarmIntentQueue.offer(intent) &&
                mAlarmIntentQueue.size() == 1) {
            SharedWakeLock.get(mContext).acquireWakeLock();
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
        if (mAlarmIntentQueue.poll() != null) {
            dispatch(mAlarmIntentQueue.peek());
        }
        if (mAlarmIntentQueue.isEmpty()) {
            SharedWakeLock.get(mContext).releaseWakeLock();
        }
    }
}
