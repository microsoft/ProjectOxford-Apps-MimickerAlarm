package com.microsoft.smartalarm;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import net.hockeyapp.android.CrashManager;

import java.util.UUID;

public class AlarmRingingActivity extends AppCompatActivity
        implements GameFactory.GameResultListener,
        ShareFragment.ShareResultListener,
        AlarmRingingFragment.RingingResultListener {

    private static final String DEFAULT_RINGING_DURATION_STRING = "60000";
    private static final int DEFAULT_RINGING_DURATION_INTEGER = 60 * 1000;
    public final String TAG = this.getClass().getSimpleName();
    private UUID mAlarmId;
    private Fragment mAlarmRingingFragment;
    private Handler mHandler;
    private Runnable mAlarmCancelTask;
    private boolean mIsGameRunning;
    private boolean mAlarmTimedOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAlarmId = (UUID) getIntent().getSerializableExtra(AlarmScheduler.ID);

        Log.d(TAG, "Creating activity!");

        AlarmUtils.setLockScreenFlags(getWindow());

        setContentView(R.layout.activity_fragment);

        mAlarmRingingFragment = AlarmRingingFragment.newInstance(mAlarmId.toString());
        showFragment(mAlarmRingingFragment);

        mAlarmCancelTask = new Runnable() {
            @Override
            public void run() {
                mAlarmTimedOut = true;
                if (!mIsGameRunning) {
                    finishActivity();
                }
            }
        };
        mHandler = new Handler();
        mHandler.postDelayed(mAlarmCancelTask, getAlarmRingingDuration());
    }

    @Override
    public void onGameSuccess(String shareable) {
        mIsGameRunning = false;
        if (shareable != null && shareable.length() > 0) {
            showFragment(ShareFragment.newInstance(shareable));
        } else {
            finishActivity();
        }
    }

    @Override
    public void onGameFailure() {
        mIsGameRunning = false;
        if (mAlarmTimedOut) {
            finishActivity();
        } else {
            showFragment(mAlarmRingingFragment);
        }
    }

    @Override
    public void onShareCompleted() {
        finishActivity();
    }

    @Override
    public void onRingingDismiss() {
        Loggable.UserAction userAction = new Loggable.UserAction(Loggable.Key.ACTION_ALARM_DISMISS);
        Alarm alarm = AlarmList.get(this).getAlarm(mAlarmId);
        userAction.putJSON(alarm.toJSON());
        Logger.track(userAction);
        mIsGameRunning = true;
        Fragment gameFragment = GameFactory.getGameFragment(this, mAlarmId);
        if (gameFragment != null) {
            showFragment(gameFragment);
        } else {
            finishActivity();
        }
    }

    @Override
    public void onRingingSnooze() {

    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(TAG, "Entered onResume!");

        final String hockeyappToken = Util.getToken(this, "hockeyapp");
        CrashManager.register(this, hockeyappToken);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "Entered onPause!");
    }

    @Override
    public void onBackPressed() {
        // Eat the back button
    }



    private void finishActivity() {
        AlarmUtils.clearLockScreenFlags(getWindow());
        SharedWakeLock.get(this).releaseWakeLock();
        mHandler.removeCallbacks(mAlarmCancelTask);
        finish();
    }

    private void showFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    private int getAlarmRingingDuration() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String durationPreference = preferences.getString("KEY_RING_DURATION", DEFAULT_RINGING_DURATION_STRING);

        int alarmRingingDuration = DEFAULT_RINGING_DURATION_INTEGER;
        try {
            alarmRingingDuration = Integer.parseInt(durationPreference);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            Logger.trackException(e);
        }

        return alarmRingingDuration;
    }
}
