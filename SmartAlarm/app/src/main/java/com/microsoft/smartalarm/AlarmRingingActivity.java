package com.microsoft.smartalarm;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import java.util.UUID;

public class AlarmRingingActivity extends AppCompatActivity
        implements GameFactory.GameResultListener,
        ShareFragment.ShareResultListener,
        AlarmRingingFragment.RingingResultListener,
        AlarmSnoozeFragment.SnoozeResultListener,
        AlarmNoGamesFragment.NoGameResultListener,
        AlarmSettingsFragment.AlarmSettingsListener {

    private static final String DEFAULT_DURATION_STRING = "60000";
    private static final int DEFAULT_DURATION_INTEGER = 60 * 1000;
    public final String TAG = this.getClass().getSimpleName();
    private UUID mAlarmId;
    private Alarm mAlarm;
    private Fragment mAlarmRingingFragment;
    private Handler mHandler;
    private Runnable mAlarmCancelTask;
    private boolean mIsGameRunning;
    private boolean mEditingSettings;
    private boolean mAlarmTimedOut;
    private AlarmRingingService mRingingService;
    private boolean mIsServiceBound;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  Because we have bound to an explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            mRingingService = ((AlarmRingingService.LocalBinder)service).getService();
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            mRingingService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAlarmId = (UUID) getIntent().getSerializableExtra(AlarmScheduler.ALARM_ID);
        mAlarm = AlarmList.get(this).getAlarm(mAlarmId);

        // Schedule the next repeating alarm if necessary
        if (!mAlarm.isOneShot()) {
            AlarmScheduler.scheduleAlarm(this, mAlarm);
        }

        Log.d(TAG, "Creating activity!");

        // This call must be made before setContentView to avoid the view being refreshed
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

        bindRingingService();
    }

    @Override
    public void onGameSuccess(String shareable) {
        cancelAlarmTimeout();
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
        Fragment gameFragment = GameFactory.getGameFragment(this, mAlarmId);
        if (gameFragment != null) {
            mIsGameRunning = true;
            showFragment(gameFragment);
        } else {
            cancelAlarmTimeout();
            showFragment(AlarmNoGamesFragment.newInstance(mAlarmId.toString()));
        }
    }

    @Override
    public void onRingingSnooze() {
        cancelAlarmTimeout();
        showFragment(new AlarmSnoozeFragment());
        AlarmScheduler.snoozeAlarm(this, mAlarm, getAlarmSnoozeDuration());
    }

    @Override
    public void onSnoozeDismiss() {
        finishActivity();
    }

    @Override
    public void onNoGameDismiss(boolean launchSettings) {
        if (launchSettings) {
            mEditingSettings = true;
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container,
                    AlarmSettingsFragment.newInstance(mAlarmId.toString()),
                    AlarmSettingsFragment.SETTINGS_FRAGMENT_TAG);
            transaction.commit();
        } else {
            finishActivity();
        }
    }

    @Override
    public void onSettingsSaveOrIgnoreChanges() {
        finishActivity();
    }

    @Override
    public void onSettingsDeleteOrNewCancel() {
        finishActivity();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(TAG, "Entered onResume!");

        Util.registerCrashReport(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "Entered onPause!");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Entered onDestroy!");
        reportRingingCompleted();
        unbindRingingService();
    }

    @Override
    public void onBackPressed() {
        if (mIsGameRunning) {
            showFragment(mAlarmRingingFragment);
        } else if (mEditingSettings) {
            AlarmSettingsFragment fragment = ((AlarmSettingsFragment)getSupportFragmentManager()
                    .findFragmentByTag(AlarmSettingsFragment.SETTINGS_FRAGMENT_TAG));
            if (fragment != null) {
                fragment.onCancel();
            }
        }
    }

    private void finishActivity() {
        AlarmUtils.clearLockScreenFlags(getWindow());
        finish();
    }

    private void showFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    private int getAlarmRingingDuration() {
        return getDurationSetting("KEY_RING_DURATION");
    }

    private int getAlarmSnoozeDuration() {
        return getDurationSetting("KEY_SNOOZE_DURATION");
    }

    private int getDurationSetting(String setting) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String durationPreference = preferences.getString(setting, DEFAULT_DURATION_STRING);

        int alarmRingingDuration = DEFAULT_DURATION_INTEGER;
        try {
            alarmRingingDuration = Integer.parseInt(durationPreference);
        } catch (NumberFormatException e) {
            Logger.trackException(e);
        }

        return alarmRingingDuration;
    }

    private void bindRingingService() {
        // Establish a connection with the service.  We use an explicit
        // class name because we want a specific service implementation that
        // we know will be running in our own process (and thus won't be
        // supporting component replacement by other applications).
        bindService(new Intent(AlarmRingingActivity.this,
                AlarmRingingService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
        mIsServiceBound = true;
    }

    private void unbindRingingService() {
        if (mIsServiceBound) {
            // Detach our existing connection.
            unbindService(mServiceConnection);
            mIsServiceBound = false;
        }
    }

    private void reportRingingCompleted () {
        if (mRingingService != null) {
            mRingingService.reportAlarmRingingCompleted();
        }
    }

    private void cancelAlarmTimeout () {
        mHandler.removeCallbacks(mAlarmCancelTask);
    }
}
