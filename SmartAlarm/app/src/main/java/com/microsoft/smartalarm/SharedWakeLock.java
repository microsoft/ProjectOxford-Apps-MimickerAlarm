package com.microsoft.smartalarm;

import android.content.Context;
import android.os.PowerManager;
import android.util.Log;


public class SharedWakeLock {
    private static final String TAG = "SharedWakeLock";
    private static SharedWakeLock sWakeLock;

    private Context mContext;
    private PowerManager.WakeLock mWakeLock;

    private SharedWakeLock(Context context) {
        mContext = context.getApplicationContext();
        PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock((PowerManager.FULL_WAKE_LOCK | PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), TAG);
    }

    // Not threadsafe
    public static SharedWakeLock get(Context context) {
        if (sWakeLock == null) {
            sWakeLock = new SharedWakeLock(context);
            Log.d(TAG, "Initialized WAKE_LOCK!");
        }
        return sWakeLock;
    }

    public void acquireWakeLock() {
        if (!mWakeLock.isHeld()) {
            mWakeLock.acquire();
            Log.d(TAG, "Acquired WAKE_LOCK!");
        }
    }

    public void releaseWakeLock() {
        if (mWakeLock.isHeld()) {
            mWakeLock.release();
            Log.d(TAG, "Released WAKE_LOCK!");
        }
    }
}
