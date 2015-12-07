package com.microsoft.smartalarm;

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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import net.hockeyapp.android.CrashManager;

import java.text.DateFormat;
import java.text.Format;
import java.util.Calendar;
import java.util.UUID;

public class AlarmRingingActivity extends AppCompatActivity {

    public final String TAG = this.getClass().getSimpleName();

    private WakeLock mWakeLock;
    private MediaPlayer mPlayer;
    private Vibrator mVibrator;
    private boolean mShouldVibrate;

    private static final String DEFAULT_RINGING_DURATION_STRING = "60000";
    private static final int DEFAULT_RINGING_DURATION_INTEGER = 60 * 1000;
    private static final int WAKE_LOCK_RELEASE_BUFFER = 3 * 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "Creating activity!");

        //Setup layout
        this.setContentView(R.layout.activity_alarm_ringing);

        final UUID id = (UUID) getIntent().getSerializableExtra(AlarmManagerHelper.ID);
        String name = getIntent().getStringExtra(AlarmManagerHelper.TITLE);
        int timeHour = getIntent().getIntExtra(AlarmManagerHelper.TIME_HOUR, 0);
        int timeMinute = getIntent().getIntExtra(AlarmManagerHelper.TIME_MINUTE, 0);
        String tone = getIntent().getStringExtra(AlarmManagerHelper.TONE);
        mShouldVibrate = getIntent().getBooleanExtra(AlarmManagerHelper.VIBRATE, false);

        TextView tvName = (TextView) findViewById(R.id.alarm_screen_name);
        tvName.setText(name);

        Format formatter = DateFormat.getTimeInstance(DateFormat.SHORT);
        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.HOUR_OF_DAY, timeHour);
        calendar.set(Calendar.MINUTE, timeMinute);

        TextView tvTime = (TextView) findViewById(R.id.alarm_screen_time);
        tvTime.setText(formatter.format(calendar.getTime()));

        Button dismissButton = (Button) findViewById(R.id.alarm_screen_button);
        dismissButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {
                if (mPlayer != null) {
                    mPlayer.stop();
                }
                Loggable.UserAction userAction = new Loggable.UserAction(Loggable.Key.ACTION_ALARM_DISMISS);
                Logger.track(userAction);
                cancelVibration();
                if (!GameFactory.startGame(AlarmRingingActivity.this, id)) {
                    finishActivity();
                }
            }
        });


        try {
            if (tone != null && !tone.equals("")) {
                Uri toneUri = Uri.parse(tone);
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

        vibrateDeviceIfDesired();

        Runnable alarmCancelTask = new Runnable() {
            @Override
            public void run() {
                if (mPlayer != null && mPlayer.isPlaying())
                {
                    mPlayer.stop();
                }
                cancelVibration();
                finish();
            }
        };

        new Handler().postDelayed(alarmCancelTask, getAlarmRingingDuration());

        Runnable releaseWakelock = new Runnable() {

            @Override
            public void run() {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

                if (mWakeLock != null && mWakeLock.isHeld()) {
                    mWakeLock.release();
                    Log.d(TAG, "Released WAKE_LOCK!");
                }
            }
        };

        new Handler().postDelayed(releaseWakelock, getAlarmRingingDuration() - WAKE_LOCK_RELEASE_BUFFER);

        setTitle(null);

        Logger.init(this);
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

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        if (mWakeLock == null) {
            mWakeLock = pm.newWakeLock((PowerManager.FULL_WAKE_LOCK | PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), TAG);
        }

        if (!mWakeLock.isHeld()) {
            mWakeLock.acquire();
            Log.d(TAG, "Acquired WAKE_LOCK!");
        }

        final String hockeyAppId = getResources().getString(R.string.hockeyapp_id);
        CrashManager.register(this, hockeyAppId);
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.d(TAG, "Entered onPause!");

        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
            Log.d(TAG, "Released WAKE_LOCK!");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GameFactory.START_GAME_REQUEST) {
            if (resultCode == RESULT_OK) {
                finishActivity();
            } else {
                if (mPlayer != null) {
                    mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            mp.start();
                        }
                    });
                    mPlayer.prepareAsync();
                }
                vibrateDeviceIfDesired();
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

    private void finishActivity() {
        if (mPlayer != null) {
            mPlayer.release();
        }
        finish();
    }
}
