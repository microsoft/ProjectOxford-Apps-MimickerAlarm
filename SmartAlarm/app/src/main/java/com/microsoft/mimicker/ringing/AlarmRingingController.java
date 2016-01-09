package com.microsoft.mimicker.ringing;

import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Vibrator;

import com.microsoft.mimicker.model.Alarm;
import com.microsoft.mimicker.model.AlarmList;
import com.microsoft.mimicker.scheduling.AlarmNotificationManager;
import com.microsoft.mimicker.scheduling.AlarmScheduler;
import com.microsoft.mimicker.utilities.Logger;
import com.microsoft.mimicker.utilities.SharedWakeLock;

import java.util.UUID;

public final class AlarmRingingController extends AlarmRingingSessionDispatcher {
    private MediaPlayer mPlayer;
    private Vibrator mVibrator;
    private Context mContext;
    private boolean mVibrating;
    private Alarm mCurrentAlarm;

    public AlarmRingingController(Context context) {
        mContext = context;
    }

    public static AlarmRingingController newInstance(Context context) {
        return new AlarmRingingController(context);
    }

    @Override
    public void beforeDispatchFirstAlarmRingingSession() {
        initializeMediaPlayer();
        initializeVibrator();
        SharedWakeLock.get(mContext).acquireFullWakeLock();
    }

    @Override
    protected void alarmRingingSessionCompleted() {
        // We need to handle the case where the alarm timed out. In that case we
        // wont get an explicit call from the AlarmRingingActivity to silence the alarm
        silenceAlarmRinging();
        mCurrentAlarm = null;
        super.alarmRingingSessionCompleted();
    }

    @Override
    public void allAlarmRingingSessionsComplete() {
        // Cleanup the state now that we are done with all ringing sessions
        mVibrator = null;
        mPlayer.release();
        mPlayer = null;
        SharedWakeLock.get(mContext).releaseFullWakeLock();
        // We should now update the notification to show the next alarm if appropriate
        AlarmRingingService.stopForegroundService(mContext);
        AlarmNotificationManager.get(mContext).handleAlarmNotificationStatus();
    }

    @Override
    public void dispatchAlarmRingingSession(Intent intent) {
        if (intent != null) {
            UUID alarmId = (UUID) intent.getExtras().getSerializable(AlarmScheduler.ALARM_ID);
            mCurrentAlarm = AlarmList.get(mContext).getAlarm(alarmId);
            startAlarmRinging();
            launchRingingUserExperience(alarmId);
            AlarmRingingService.startForegroundService(mContext, alarmId, 0,
                    AlarmNotificationManager.NOTIFICATION_ALARM_RUNNING);
        }
    }

    public void silenceAlarmRinging() {
        handleVibration(false);
        handleAlarmSound(null);
    }

    public void startAlarmRinging() {
        handleVibration(mCurrentAlarm.shouldVibrate());
        handleAlarmSound(mCurrentAlarm.getAlarmTone());
    }

    // alarmRingingSessionCompleted should always be called before this method.  If not, we should
    // restart the AlarmRingingActivity so that we can successfully finish the alarm session
    public void alarmRingingSessionDismissed() {
        if (mCurrentAlarm != null) {
            launchRingingUserExperience(mCurrentAlarm.getId());
        }
    }

    private void launchRingingUserExperience(UUID alarmId) {
        Intent ringingIntent = new Intent(mContext, AlarmRingingActivity.class);
        ringingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        ringingIntent.putExtra(AlarmRingingService.ALARM_ID, alarmId);
        mContext.startActivity(ringingIntent);
    }

    private void initializeVibrator() {
        mVibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
    }

    private void handleVibration(boolean shouldVibrate) {
        if (shouldVibrate) {
            if (!mVibrating) {
                vibrateDevice();
            }
        } else {
            if (mVibrating) {
                cancelVibration();
            }
        }
    }

    private void vibrateDevice() {
        // Start immediately
        // Vibrate for 200 milliseconds
        // Sleep for 500 milliseconds
        long[] vibrationPattern = {0, 200, 500};
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            mVibrator.vibrate(vibrationPattern, 0,
                    new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build());
        } else {
            mVibrator.vibrate(vibrationPattern, 0);
        }
        mVibrating = true;
    }

    private void cancelVibration() {
        mVibrator.cancel();
        mVibrating = false;
    }

    private void initializeMediaPlayer() {
        try {
            mPlayer = new MediaPlayer();
            mPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
            mPlayer.setLooping(true);
        } catch (Exception e) {
            Logger.trackException(e);
        }
    }

    private void handleAlarmSound(Uri toneUri) {
        if (toneUri != null) {
            cancelAlarmSound();
            playAlarmSound(toneUri);
        } else {
            cancelAlarmSound();
        }
    }

    private void playAlarmSound(Uri toneUri) {
        try {
            if (mPlayer != null && !mPlayer.isPlaying()) {
                mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mp.start();
                    }
                });
                mPlayer.setDataSource(mContext, toneUri);
                mPlayer.prepareAsync();
            }
        } catch (Exception e) {
            Logger.trackException(e);
        }
    }

    private void cancelAlarmSound() {
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.stop();
            mPlayer.reset();
        }
    }
}
