package com.microsoft.smartalarm;

import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import net.hockeyapp.android.CrashManager;

public class AlarmRingingActivity extends AppCompatActivity
        implements GameTwisterFragment.GameResultListener {

    public final String TAG = this.getClass().getSimpleName();

    private WakeLock mWakeLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "Creating activity!");

        setTitle(null);

        setContentView(R.layout.activity_fragment);


    }

    @Override
    public void onGameSuccess() {

    }

    @Override
    public void onGameFailure() {

    }

    private void dismissAlarm() {
/*
        Loggable.UserAction userAction = new Loggable.UserAction(Loggable.Key.ACTION_ALARM_DISMISS);
        Alarm alarm = AlarmList.get(this).getAlarm(mAlarmId);
        userAction.putJSON(alarm.toJSON());
        Logger.track(userAction);

        if (!GameFactory.startGame(AlarmRingingFragment.this, mAlarmId)) {
            finishActivity();
        }
        */
    }

    private void snoozeAlarm() {
        // TODO - Oxford Apps VSO Task: 5264 Enable snooze functionality on ringing screen
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(TAG, "Entered onResume!");

        AlarmUtils.setLockScreenFlags(getWindow());
        acquireWakeLock();

        final String hockeyappToken = Util.getToken(this, "hockeyapp");
        CrashManager.register(this, hockeyappToken);
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.d(TAG, "Entered onPause!");

        releaseWakeLock();
    }

    @Override
    public void onBackPressed() {
        // Eat the back button
    }

    private void acquireWakeLock() {
        PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        if (mWakeLock == null) {
            mWakeLock = pm.newWakeLock((PowerManager.FULL_WAKE_LOCK | PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), TAG);
        }

        if (!mWakeLock.isHeld()) {
            mWakeLock.acquire();
            Log.d(TAG, "Acquired WAKE_LOCK!");
        }
    }

    private void releaseWakeLock() {
        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
            Log.d(TAG, "Released WAKE_LOCK!");
        }
    }

    private void finishActivity() {

        finish();
    }
}
