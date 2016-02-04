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

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.microsoft.mimickeralarm.R;
import com.microsoft.mimickeralarm.mimics.MimicFactory;
import com.microsoft.mimickeralarm.mimics.MimicNoNetworkFragment;
import com.microsoft.mimickeralarm.model.Alarm;
import com.microsoft.mimickeralarm.model.AlarmList;
import com.microsoft.mimickeralarm.scheduling.AlarmScheduler;
import com.microsoft.mimickeralarm.settings.AlarmSettingsFragment;
import com.microsoft.mimickeralarm.settings.MimicsPreference;
import com.microsoft.mimickeralarm.settings.MimicsSettingsFragment;
import com.microsoft.mimickeralarm.utilities.GeneralUtilities;
import com.microsoft.mimickeralarm.utilities.SettingsUtilities;
import com.microsoft.mimickeralarm.utilities.SharedWakeLock;

import java.util.ArrayList;
import java.util.UUID;

/**
 * This class controls all the alarm ringing experience from a user experience perspective.  It
 * hosts the main ringing fragment (AlarmRingingFragment) and transitions to further screens based
 * on the choice of the user.
 *
 * On ringing of an alarm a user has the choice to snooze or dismiss the alarm.  In the dismiss
 * case a random Mimic fragment will be launched, but if no Mimics are selected the user will see
 * the No Mimics screen (AlarmNoMimicsFragment).
 *
 * This activity is started as a new task by the AlarmRingingController.  The activity reports to
 * the controller - via bound calls to the AlarmRingingService - the state of the ringing user
 * experience.
 *
 * All the hosted fragments communicate to the activity via callback listener interfaces.
 *
 * This activity has a launch mode of singleTask, as we do not want more than one instance to be
 * launched at a time.
 */
public class AlarmRingingActivity extends AppCompatActivity
        implements MimicFactory.MimicResultListener,
        ShareFragment.ShareResultListener,
        AlarmRingingFragment.RingingResultListener,
        AlarmSnoozeFragment.SnoozeResultListener,
        AlarmNoMimicsFragment.NoMimicResultListener,
        AlarmSettingsFragment.AlarmSettingsListener,
        MimicsSettingsFragment.MimicsSettingsListener {


    private static final int ALARM_DURATION_INTEGER = (2 * 60 * 60) * 1000;
    public final String TAG = this.getClass().getSimpleName();
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

    private BroadcastReceiver mScreenReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                // If the alarm is ringing, stop playing the sound and vibrating. The vibrator may
                // already have already been cancelled by the screen turning off. We do this
                // so that we can have a clean restart of vibration and sound playing after turning
                // on the screen again.
                if (isAlarmRinging()) {
                    notifyControllerSilenceAlarmRinging();
                }

                // We release and reacquire the wakelock so that we can turn the screen back on
                SharedWakeLock.get(getApplicationContext()).releaseFullWakeLock();
                SharedWakeLock.get(getApplicationContext()).acquireFullWakeLock();

                // Restart the alarm and vibrator playing if they were both turned off
                if (isAlarmRinging()) {
                    notifyControllerStartAlarmRinging();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UUID alarmId = (UUID) getIntent().getSerializableExtra(AlarmScheduler.ARGS_ALARM_ID);
        mAlarm = AlarmList.get(this).getAlarm(alarmId);

        Log.d(TAG, "Creating activity!");

        // This call must be made before setContentView to avoid the view being refreshed
        GeneralUtilities.setLockScreenFlags(getWindow());

        setContentView(R.layout.activity_fragment);

        mAlarmRingingFragment = AlarmRingingFragment.newInstance(mAlarm.getId().toString());

        // We do not want any animations when the ringing fragment is launched for the first time
        GeneralUtilities.showFragment(getSupportFragmentManager(),
                mAlarmRingingFragment,
                AlarmRingingFragment.RINGING_FRAGMENT_TAG);

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

        registerReceiver(mScreenReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));

        bindRingingService();
    }

    @Override
    public void onMimicSuccess(String shareable) {
        mAlarm.onDismiss();
        cancelAlarmTimeout();
        if (shareable != null && shareable.length() > 0) {
            GeneralUtilities.showFragmentFromRight(getSupportFragmentManager(),
                    ShareFragment.newInstance(shareable),
                    ShareFragment.SHARE_FRAGMENT_TAG);
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
    public void onMimicError() {
        Toast.makeText(this, getString(R.string.mimic_error_toast), Toast.LENGTH_SHORT).show();
        GeneralUtilities.showFragmentFromRight(getSupportFragmentManager(),
                MimicFactory.getNoNetworkMimic(this),
                MimicNoNetworkFragment.NO_NETWORK_FRAGMENT_TAG);
    }

    @Override
    public void onShareCompleted() {
        finishActivity();
    }

    @Override
    public void onRequestLaunchShareAction() {
        notifyControllerAllowDismiss();
    }

    @Override
    public void onRingingDismiss() {
        notifyControllerSilenceAlarmRinging();
        Fragment mimicFragment = MimicFactory.getMimicFragment(this, mAlarm.getId());
        if (mimicFragment != null) {
            GeneralUtilities.showFragmentFromRight(getSupportFragmentManager(),
                    mimicFragment, MimicFactory.MIMIC_FRAGMENT_TAG);
        } else {
            mAlarm.onDismiss();
            cancelAlarmTimeout();
            GeneralUtilities.showFragmentFromRight(getSupportFragmentManager(),
                    AlarmNoMimicsFragment.newInstance(mAlarm.getId().toString()),
                    AlarmNoMimicsFragment.NO_MIMICS_FRAGMENT_TAG);
        }
    }

    @Override
    public void onRingingSnooze() {
        notifyControllerSilenceAlarmRinging();
        cancelAlarmTimeout();
        mAlarm.snooze();
        // Show the snooze user interface
        GeneralUtilities.showFragmentFromLeft(getSupportFragmentManager(),
                new AlarmSnoozeFragment(),
                AlarmSnoozeFragment.SNOOZE_FRAGMENT_TAG);
    }

    @Override
    public void onSnoozeDismiss() {
        finishActivity();
    }

    @Override
    public void onNoMimicDismiss(boolean launchSettings) {
        if (launchSettings) {
            GeneralUtilities.showFragmentFromRight(getSupportFragmentManager(),
                    MimicsSettingsFragment.newInstance(
                            MimicsPreference.getEnabledMimics(this, mAlarm)),
                    MimicsSettingsFragment.MIMICS_SETTINGS_FRAGMENT_TAG);
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
    public void onMimicsSettingsDismiss(ArrayList<String> enabledMimics) {
        // If Mimics settings was launched from Alarm settings just update Alarm settings,
        // otherwise we need to launch Alarm settings
        AlarmSettingsFragment settingsFragment = SettingsUtilities
                .getAlarmSettingsFragment(getSupportFragmentManager());
        if (settingsFragment != null){
            settingsFragment.updateMimicsPreference(enabledMimics);
        } else {
            GeneralUtilities.showFragmentFromLeft(getSupportFragmentManager(),
                    AlarmSettingsFragment.newInstance(mAlarm.getId().toString(), enabledMimics),
                    AlarmSettingsFragment.SETTINGS_FRAGMENT_TAG);
        }
    }

    @Override
    public void onShowMimicsSettings(ArrayList<String> enabledMimics) {
        SettingsUtilities.transitionFromAlarmToMimicsSettings(getSupportFragmentManager(),
                enabledMimics);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(TAG, "Entered onResume!");

        GeneralUtilities.registerCrashReport(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "Entered onPause!");
        notifyControllerRingingDismissed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Entered onDestroy!");
        unregisterReceiver(mScreenReceiver);
        unbindRingingService();
    }

    @Override
    public void onBackPressed() {
        if (isGameRunning()) {
            transitionBackToRingingScreen();
        } else if (SettingsUtilities.areEditingSettings(getSupportFragmentManager())) {
            if (SettingsUtilities.areEditingAlarmSettingsExclusive(getSupportFragmentManager())) {
                // We always need to handle the case where there are non-persisted settings
                SettingsUtilities.getAlarmSettingsFragment(getSupportFragmentManager()).onCancel();
            } else if (SettingsUtilities.areEditingMimicsSettingsExclusive(getSupportFragmentManager())) {
                // This is the scenario where we were launched from the NoMimics fragment
                SettingsUtilities.getMimicsSettingsFragment(getSupportFragmentManager()).onBack();
            } else {
                // This implies we are in the Mimics settings and we were launched from Alarm
                // settings.  In this case we just pop the stack.
                super.onBackPressed();
            }
        } else if (!isAlarmRinging()) {
            finishActivity();
        }
    }

    private void finishActivity() {
        // We only want to report that ringing completed as a result of correct user action
        notifyControllerRingingCompleted();
        finish();
    }

    private void transitionBackToRingingScreen() {
        GeneralUtilities.showFragmentFromLeft(getSupportFragmentManager(),
                mAlarmRingingFragment,
                AlarmRingingFragment.RINGING_FRAGMENT_TAG);
        notifyControllerStartAlarmRinging();
    }

    private boolean isAlarmRinging() {
        return (getSupportFragmentManager()
                .findFragmentByTag(AlarmRingingFragment.RINGING_FRAGMENT_TAG) != null);
    }

    private boolean isGameRunning() {
        return (getSupportFragmentManager()
                .findFragmentByTag(MimicFactory.MIMIC_FRAGMENT_TAG) != null);
    }

    private int getAlarmRingingDuration() {
        return GeneralUtilities.getDurationSetting(R.string.pref_ring_duration_key,
                R.string.pref_default_ring_duration_value,
                ALARM_DURATION_INTEGER);
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

    private void notifyControllerRingingCompleted() {
        if (mRingingService != null) {
            mRingingService.reportAlarmUXCompleted();
        }
    }

    private void notifyControllerSilenceAlarmRinging() {
        if (mRingingService != null) {
            mRingingService.silenceAlarmRinging();
        }
    }

    private void notifyControllerStartAlarmRinging() {
        if (mRingingService != null) {
            mRingingService.startAlarmRinging();
        }
    }

    private void notifyControllerRingingDismissed() {
        if (mRingingService != null) {
            mRingingService.reportAlarmUXDismissed();
        }
    }

    private void notifyControllerAllowDismiss() {
        if (mRingingService != null) {
            mRingingService.requestAllowUXDismiss();
        }
    }

    private void cancelAlarmTimeout () {
        mHandler.removeCallbacks(mAlarmCancelTask);
    }
}
