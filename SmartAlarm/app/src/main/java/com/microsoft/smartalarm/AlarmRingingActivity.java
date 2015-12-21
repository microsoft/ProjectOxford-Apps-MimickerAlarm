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

import net.hockeyapp.android.CrashManager;

import java.util.UUID;

public class AlarmRingingActivity extends AppCompatActivity
        implements GameFactory.GameResultListener,
        ShareFragment.ShareResultListener,
        AlarmRingingFragment.RingingResultListener,
        AlarmSnoozeFragment.SnoozeResultListener {

    private static final String DEFAULT_SNOOZE_DURATION_STRING = "60000";
    private static final int DEFAULT_SNOOZE_DURATION_INTEGER = 60 * 1000;
    private static final String DEFAULT_RINGING_DURATION_STRING = "60000";
    private static final int DEFAULT_RINGING_DURATION_INTEGER = 60 * 1000;
    public final String TAG = this.getClass().getSimpleName();
    private UUID mAlarmId;
    private Alarm mAlarm;
    private Fragment mAlarmRingingFragment;
    private Handler mHandler;
    private Runnable mAlarmCancelTask;
    private boolean mIsGameRunning;
    private boolean mAlarmTimedOut;
    private AlarmRingingService mRingingService;
    private boolean mIsServiceBound;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  Because we have bound to a explicit
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

        mAlarmId = (UUID) getIntent().getSerializableExtra(AlarmScheduler.ID);
        mAlarm = AlarmList.get(this).getAlarm(mAlarmId);

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

        bindRingingService();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
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
        userAction.putJSON(mAlarm.toJSON());
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
        showFragment(new AlarmSnoozeFragment());
        AlarmScheduler.snoozeAlarm(this, mAlarm, getAlarmSnoozeDuration());
    }

    @Override
    public void onSnoozeDismiss() {
        finishActivity();
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
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Entered onDestroy!");
        reportRingingCompleted();
        unbindRingingService();
    }

    @Override
    public void onBackPressed() {
        // Eat the back button
    }

    private void finishActivity() {
        AlarmUtils.clearLockScreenFlags(getWindow());
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
            Logger.trackException(e);
        }

        return alarmRingingDuration;
    }

    private int getAlarmSnoozeDuration() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String durationPreference = preferences.getString("KEY_SNOOZE_DURATION", DEFAULT_SNOOZE_DURATION_STRING);

        int alarmRingingDuration = DEFAULT_SNOOZE_DURATION_INTEGER;
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
}
