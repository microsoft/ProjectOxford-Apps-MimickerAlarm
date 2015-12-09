package com.microsoft.smartalarm;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.ClipData;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Vibrator;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.animation.BounceInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import net.hockeyapp.android.CrashManager;

import java.util.UUID;

public class AlarmRingingActivity extends AppCompatActivity {

    public final String TAG = this.getClass().getSimpleName();

    private WakeLock mWakeLock;
    private MediaPlayer mPlayer;
    private Vibrator mVibrator;
    private boolean mShouldVibrate;
    private ImageView mAlarmRingingClock;
    private UUID mAlarmId;
    private String mAlarmTone;
    private boolean mShowClockOnDragEnd = true;
    private Handler mHandler;
    private Runnable mAlarmCancelTask;

    private static final String DEFAULT_RINGING_DURATION_STRING = "60000";
    private static final int DEFAULT_RINGING_DURATION_INTEGER = 60 * 1000;
    private static final int CLOCK_ANIMATION_DURATION = 1500;
    private static final int SHOW_CLOCK_AFTER_UNSUCCESSFUL_DRAG_DELAY = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Logger.init(this);

        Log.d(TAG, "Creating activity!");

        mAlarmId = (UUID) getIntent().getSerializableExtra(AlarmManagerHelper.ID);
        String name = getIntent().getStringExtra(AlarmManagerHelper.TITLE);
        int timeHour = getIntent().getIntExtra(AlarmManagerHelper.TIME_HOUR, 0);
        int timeMinute = getIntent().getIntExtra(AlarmManagerHelper.TIME_MINUTE, 0);
        mAlarmTone = getIntent().getStringExtra(AlarmManagerHelper.TONE);
        mShouldVibrate = getIntent().getBooleanExtra(AlarmManagerHelper.VIBRATE, false);

        setTitle(null);

        setContentView(R.layout.activity_alarm_ringing);

        TextView timeField = (TextView) findViewById(R.id.alarm_ringing_time);
        timeField.setText(AlarmUtils.getUserTimeString(getApplicationContext(), timeHour, timeMinute));

        TextView dateField = (TextView) findViewById(R.id.alarm_ringing_date);
        dateField.setText(AlarmUtils.getFullDateStringForNow());

        if (name == null || name.isEmpty()) {
            name = getString(R.string.alarm_ringing_default_text);
        }
        TextView titleField = (TextView) findViewById(R.id.alarm_ringing_title);
        titleField.setText(name);

        ImageView dismissButton = (ImageView) findViewById(R.id.alarm_ringing_dismiss);
        dismissButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                dismissAlarm();
            }
        });
        dismissButton.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                switch (event.getAction()) {
                    case DragEvent.ACTION_DROP:
                        dismissAlarm();
                        break;
                    case DragEvent.ACTION_DRAG_ENDED:
                        if (mShowClockOnDragEnd) {
                            mAlarmRingingClock.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mAlarmRingingClock.setVisibility(View.VISIBLE);
                                }
                            }, SHOW_CLOCK_AFTER_UNSUCCESSFUL_DRAG_DELAY);
                        }
                        break;
                    default:
                        break;
                }
                return true;
            }
        });

        ImageView snoozeButton = (ImageView) findViewById(R.id.alarm_ringing_snooze);
        snoozeButton.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                switch(event.getAction()) {
                    case DragEvent.ACTION_DROP:
                        snoozeAlarm();
                        break;
                    default:
                        break;
                }
                return true;
            }
        });

        mAlarmRingingClock = (ImageView) findViewById(R.id.alarm_ringing_clock);
        mAlarmRingingClock.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    ClipData dragData = ClipData.newPlainText("", "");
                    View.DragShadowBuilder shadow = new View.DragShadowBuilder(mAlarmRingingClock);
                    mAlarmRingingClock.startDrag(dragData, shadow, null, 0);
                    mAlarmRingingClock.setVisibility(View.INVISIBLE);
                    return true;
                } else {
                    return false;
                }

            }
        });

        animateClock();
        playAlarmSound();
        vibrateDeviceIfDesired();

        mAlarmCancelTask = new Runnable() {
            @Override
            public void run() {
                cancelAlarmSound();
                cancelVibration();
                clearLockScreenFlags();
                releaseWakeLock();
                finishActivity();
            }
        };

        mHandler = new Handler();
        mHandler.postDelayed(mAlarmCancelTask, getAlarmRingingDuration());

        Loggable.AppAction appAction = new Loggable.AppAction(Loggable.Key.APP_ALARM_RINGING);
        Alarm alarm = AlarmList.get(this).getAlarm(mAlarmId);
        appAction.putJSON(alarm.toJSON());
        Logger.track(appAction);
    }

    private void dismissAlarm() {
        mShowClockOnDragEnd = false;

        Loggable.UserAction userAction = new Loggable.UserAction(Loggable.Key.ACTION_ALARM_DISMISS);
        Alarm alarm = AlarmList.get(this).getAlarm(mAlarmId);
        userAction.putJSON(alarm.toJSON());
        Logger.track(userAction);

        cancelAlarmSound();
        cancelVibration();

        if (!GameFactory.startGame(AlarmRingingActivity.this, mAlarmId)) {
            finishActivity();
        }
    }

    private void snoozeAlarm() {
        // TODO - Oxford Apps VSO Task: 5264 Enable snooze functionality on ringing screen
        mAlarmRingingClock.post(new Runnable() {
            @Override
            public void run() {
                Snackbar.make(mAlarmRingingClock, "Snooze functionality coming soon!", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d(TAG, "OnNewIntent - Received a new intent!");
        // TODO Figure out what the behaviour is for when two alarms overlap
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(TAG, "Entered onResume!");

        setLockScreenFlags();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GameFactory.START_GAME_REQUEST) {
            if (resultCode == RESULT_OK) {
                finishActivity();
            } else {
                restartAlarmSound();
                vibrateDeviceIfDesired();
                mShowClockOnDragEnd = true;
                mAlarmRingingClock.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onBackPressed() {
        // Eat the back button
    }

    private int getAlarmRingingDuration() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String durationPreference = preferences.getString("KEY_RING_DURATION", DEFAULT_RINGING_DURATION_STRING);

        int alarmRingingDuration = DEFAULT_RINGING_DURATION_INTEGER;
        try {
            alarmRingingDuration = Integer.parseInt(durationPreference);
        } catch (NumberFormatException e){
            e.printStackTrace();
            Logger.trackException(e);
        }

        return alarmRingingDuration;
    }

    private void vibrateDeviceIfDesired() {
        if (mShouldVibrate) {
            mVibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
            // Start immediately
            // Vibrate for 200 milliseconds
            // Sleep for 500 milliseconds
            long[] vibrationPattern = { 0, 200, 500 };
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                mVibrator.vibrate(vibrationPattern, 0, new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build());
            } else {
                mVibrator.vibrate(vibrationPattern, 0);
            }
        }
    }

    private void cancelVibration() {
        if (mVibrator != null) {
            mVibrator.cancel();
        }
    }

    private void playAlarmSound() {
        try {
            if (mAlarmTone != null && !mAlarmTone.isEmpty()) {
                Uri toneUri = Uri.parse(mAlarmTone);
                if (toneUri != null) {
                    mPlayer = new MediaPlayer();
                    mPlayer.setDataSource(this, toneUri);
                    mPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                    mPlayer.setLooping(true);
                    mPlayer.prepare();
                    mPlayer.start();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Logger.trackException(e);
        }
    }

    private void restartAlarmSound() {
        if (mPlayer != null) {
            mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                }
            });
            mPlayer.prepareAsync();
        }
    }

    private void cancelAlarmSound() {
        if (mPlayer != null && mPlayer.isPlaying())
        {
            mPlayer.stop();
        }
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

    private void setLockScreenFlags() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
    }

    private void clearLockScreenFlags() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
    }

    private void animateClock() {
        ObjectAnimator animateClock = ObjectAnimator.ofFloat(mAlarmRingingClock, "translationY", -35f, 0f);
        animateClock.setDuration(CLOCK_ANIMATION_DURATION);
        animateClock.setInterpolator(new BounceInterpolator());
        animateClock.setRepeatCount(ValueAnimator.INFINITE);
        animateClock.start();
    }

    private void finishActivity() {
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }

        mHandler.removeCallbacks(mAlarmCancelTask);

        finish();
    }
}
