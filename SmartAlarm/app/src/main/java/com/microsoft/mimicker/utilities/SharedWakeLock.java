package com.microsoft.mimicker.utilities;

import android.content.Context;
import android.os.PowerManager;
import android.util.Log;

public class SharedWakeLock {
    private static final String TAG = "SharedWakeLock";
    private static SharedWakeLock sWakeLock;

    private PowerManager.WakeLock mFullWakeLock;
    private PowerManager.WakeLock mPartialWakeLock;

    @SuppressWarnings("deprecation")
    private SharedWakeLock(Context context) {
        Context appContext = context.getApplicationContext();
        PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
        mFullWakeLock = pm.newWakeLock((PowerManager.FULL_WAKE_LOCK | PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), TAG);
        mPartialWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
    }

    // Not threadsafe
    public static SharedWakeLock get(Context context) {
        if (sWakeLock == null) {
            sWakeLock = new SharedWakeLock(context);
            Log.d(TAG, "Initialized shared WAKE_LOCKs!");
        }
        return sWakeLock;
    }

    public void acquireFullWakeLock() {
        if (!mFullWakeLock.isHeld()) {
            mFullWakeLock.acquire();
            Log.d(TAG, "Acquired Full WAKE_LOCK!");
        }
    }

    public void releaseFullWakeLock() {
        if (mFullWakeLock.isHeld()) {
            mFullWakeLock.release();
            Log.d(TAG, "Released Full WAKE_LOCK!");
        }
    }

    public void acquirePartialWakeLock() {
        if (!mPartialWakeLock.isHeld()) {
            mPartialWakeLock.acquire();
            Log.d(TAG, "Acquired Partial WAKE_LOCK!");
        }
    }

    public void releasePartialWakeLock() {
        if (mPartialWakeLock.isHeld()) {
            mPartialWakeLock.release();
            Log.d(TAG, "Released Partial WAKE_LOCK!");
        }
    }


}
