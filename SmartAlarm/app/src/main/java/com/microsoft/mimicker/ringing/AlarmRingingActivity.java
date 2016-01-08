package com.microsoft.mimicker.ringing;

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

import com.microsoft.mimicker.R;
import com.microsoft.mimicker.mimics.MimicFactory;
import com.microsoft.mimicker.model.Alarm;
import com.microsoft.mimicker.model.AlarmList;
import com.microsoft.mimicker.scheduling.AlarmScheduler;
import com.microsoft.mimicker.settings.AlarmSettingsFragment;
import com.microsoft.mimicker.utilities.AlarmUtils;
import com.microsoft.mimicker.utilities.Logger;
import com.microsoft.mimicker.utilities.Util;

import java.util.UUID;

public class AlarmRingingActivity extends AppCompatActivity
        implements MimicFactory.MimicResultListener,
        ShareFragment.ShareResultListener,
        AlarmRingingFragment.RingingResultListener,
        AlarmSnoozeFragment.SnoozeResultListener,
        AlarmNoMimicsFragment.NoMimicResultListener,
        AlarmSettingsFragment.AlarmSettingsListener {

    private static final int DEFAULT_DURATION_INTEGER = (10 * 60) * 1000;
    public final String TAG = this.getClass().getSimpleName();
    private UUID mAlarmId;
    private Alarm mAlarm;
    private Fragment mAlarmRingingFragment;
    private Handler mHandler;
    private Runnable mAlarmCancelTask;
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

        // We do not want any animations when the ringing fragment is launched for the first time
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, mAlarmRingingFragment,
                AlarmRingingFragment.RINGING_FRAGMENT_TAG);
        transaction.commit();

        mAlarmCancelTask = new Runnable() {
            @Override
            public void run() {
                mAlarmTimedOut = true;
                if (!isGameRunning()) {
                    finishActivity();
                }
            }
        };
        mHandler = new Handler();
        int ringingDuration = getAlarmRingingDuration();
        if (ringingDuration > 0) {
            mHandler.postDelayed(mAlarmCancelTask, ringingDuration);
        }

        bindRingingService();
    }

    @Override
    public void onMimicSuccess(String shareable) {
        cancelAlarmTimeout();
        if (shareable != null && shareable.length() > 0) {
            showFragment(ShareFragment.newInstance(shareable), ShareFragment.SHARE_FRAGMENT_TAG);
        } else {
            finishActivity();
        }
    }

    @Override
    public void onMimicFailure() {
        if (mAlarmTimedOut) {
            finishActivity();
        } else {
            transitionBackToRingingScreen();
        }
    }

    @Override
    public void onShareCompleted() {
        finishActivity();
    }

    @Override
    public void onRingingDismiss() {
        silenceAlarmRinging();
        Fragment mimicFragment = MimicFactory.getMimicFragment(this, mAlarmId);
        if (mimicFragment != null) {
            showFragment(mimicFragment, MimicFactory.MIMIC_FRAGMENT_TAG);
        } else {
            cancelAlarmTimeout();
            showFragment(AlarmNoMimicsFragment.newInstance(mAlarmId.toString()),
                    AlarmNoMimicsFragment.NO_MIMICS_FRAGMENT_TAG);
        }
    }

    @Override
    public void onRingingSnooze() {
        silenceAlarmRinging();
        cancelAlarmTimeout();
        showFragment(new AlarmSnoozeFragment(), AlarmSnoozeFragment.SNOOZE_FRAGMENT_TAG);
        AlarmScheduler.snoozeAlarm(this, mAlarm, getAlarmSnoozeDuration());
    }

    @Override
    public void onSnoozeDismiss() {
        finishActivity();
    }

    @Override
    public void onNoMimicDismiss(boolean launchSettings) {
        if (launchSettings) {
            showFragment(AlarmSettingsFragment.newInstance(mAlarmId.toString()),
                    AlarmSettingsFragment.SETTINGS_FRAGMENT_TAG);
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
        reportRingingDismissed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Entered onDestroy!");
        unbindRingingService();
    }

    @Override
    public void onBackPressed() {
        if (isGameRunning()) {
            transitionBackToRingingScreen();
        } else if (areEditingSettings()) {
            ((AlarmSettingsFragment) getSupportFragmentManager()
                    .findFragmentByTag(AlarmSettingsFragment.SETTINGS_FRAGMENT_TAG))
                    .onCancel();
        } else if (!isAlarmRinging()) {
            finishActivity();
        }
    }

    private void finishActivity() {
        // We only want to report that ringing completed as a result of correct user action
        reportRingingCompleted();
        finish();
    }

    private void showFragment(Fragment fragment, String fragmentTag) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
        transaction.replace(R.id.fragment_container, fragment, fragmentTag);
        transaction.commit();
    }

    private void transitionBackToRingingScreen() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
        transaction.replace(R.id.fragment_container, mAlarmRingingFragment,
                AlarmRingingFragment.RINGING_FRAGMENT_TAG);
        transaction.commit();
        startAlarmRinging();
    }

    private boolean isAlarmRinging() {
        return (getSupportFragmentManager()
                .findFragmentByTag(AlarmRingingFragment.RINGING_FRAGMENT_TAG) != null);
    }

    private boolean isGameRunning() {
        return (getSupportFragmentManager()
                .findFragmentByTag(MimicFactory.MIMIC_FRAGMENT_TAG) != null);
    }

    private boolean areEditingSettings() {
        return (getSupportFragmentManager()
                .findFragmentByTag(AlarmSettingsFragment.SETTINGS_FRAGMENT_TAG) != null);
    }

    private int getAlarmRingingDuration() {
        return getDurationSetting(getString(R.string.pref_ring_duration_key),
                R.string.pref_default_ring_duration_value);
    }

    private int getAlarmSnoozeDuration() {
        return getDurationSetting(getString(R.string.pref_snooze_duration_key),
                R.string.pref_default_snooze_duration_value);
    }

    private int getDurationSetting(String setting, int defaultSettingStringResId) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String durationPreference = preferences.getString(setting, getString(defaultSettingStringResId));

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

    private void reportRingingCompleted() {
        if (mRingingService != null) {
            mRingingService.reportAlarmUXCompleted();
        }
    }

    private void silenceAlarmRinging() {
        if (mRingingService != null) {
            mRingingService.silenceAlarmRinging();
        }
    }

    private void startAlarmRinging() {
        if (mRingingService != null) {
            mRingingService.startAlarmRinging();
        }
    }

    private void reportRingingDismissed() {
        if (mRingingService != null) {
            mRingingService.reportAlarmUXDismissed();
        }
    }

    private void cancelAlarmTimeout () {
        mHandler.removeCallbacks(mAlarmCancelTask);
    }
}
