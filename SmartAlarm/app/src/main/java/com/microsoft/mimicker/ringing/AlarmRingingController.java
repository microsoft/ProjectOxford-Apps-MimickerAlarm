package com.microsoft.mimicker.ringing;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.microsoft.mimicker.model.Alarm;
import com.microsoft.mimicker.model.AlarmList;
import com.microsoft.mimicker.scheduling.AlarmNotificationManager;
import com.microsoft.mimicker.scheduling.AlarmScheduler;
import com.microsoft.mimicker.utilities.SharedWakeLock;

import java.util.UUID;

public final class AlarmRingingController extends AlarmRingingSessionDispatcher {
    private Context mContext;
    private AlarmRingtonePlayer mRingtonePlayer;
    private AlarmVibrator mVibrator;
    private Alarm mCurrentAlarm;
    private boolean mAllowDismissRequested;

    public AlarmRingingController(Context context) {
        mContext = context;
        mRingtonePlayer = new AlarmRingtonePlayer(mContext);
        mVibrator = new AlarmVibrator(mContext);
    }

    public static AlarmRingingController newInstance(Context context) {
        return new AlarmRingingController(context);
    }

    @Override
    public void beforeDispatchFirstAlarmRingingSession() {
        mRingtonePlayer.initialize();
        mVibrator.initialize();
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
        mVibrator.cleanup();
        mRingtonePlayer.cleanup();

        SharedWakeLock.get(mContext).releaseFullWakeLock();
        // We should now update the notification to show the next alarm if appropriate
        AlarmNotificationManager.get(mContext).handleNextAlarmNotificationStatus();
    }

    @Override
    public void dispatchAlarmRingingSession(Intent intent) {
        if (intent != null) {
            UUID alarmId = (UUID) intent.getExtras().getSerializable(AlarmScheduler.ARGS_ALARM_ID);
            mCurrentAlarm = AlarmList.get(mContext).getAlarm(alarmId);
            startAlarmRinging();
            launchRingingUserExperience(alarmId);
            AlarmNotificationManager.get(mContext).handleAlarmRunningNotificationStatus(alarmId);
        }
    }

    public void silenceAlarmRinging() {
        mVibrator.stop();
        mRingtonePlayer.stop();
    }

    public void startAlarmRinging() {
        if (mCurrentAlarm.shouldVibrate()) {
            mVibrator.vibrate();
        }
        Uri ringtone = mCurrentAlarm.getAlarmTone();
        if (ringtone != null) {
            mRingtonePlayer.play(ringtone);
        }
    }

    public void requestAllowDismiss() {
        mAllowDismissRequested = true;
    }

    // alarmRingingSessionCompleted should always be called before this method.  If not, we should
    // restart the AlarmRingingActivity so that we can successfully finish the alarm session
    public void alarmRingingSessionDismissed() {
        if (mAllowDismissRequested) {
            mAllowDismissRequested = false;
        } else {
            if (mCurrentAlarm != null) {
                launchRingingUserExperience(mCurrentAlarm.getId());
            }
        }
    }

    private void launchRingingUserExperience(UUID alarmId) {
        Intent ringingIntent = new Intent(mContext, AlarmRingingActivity.class);
        ringingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        ringingIntent.putExtra(AlarmRingingService.ALARM_ID, alarmId);
        mContext.startActivity(ringingIntent);
    }
}
