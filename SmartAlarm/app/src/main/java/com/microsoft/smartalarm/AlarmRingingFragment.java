package com.microsoft.smartalarm;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.ClipData;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.UUID;

public class AlarmRingingFragment extends Fragment {
    private static final String ARGS_ALARM_ID = "alarm_id";
    private static final int CLOCK_ANIMATION_DURATION = 1500;
    private static final int SHOW_CLOCK_AFTER_UNSUCCESSFUL_DRAG_DELAY = 250;
    public final String TAG = this.getClass().getSimpleName();
    RingingResultListener mCallback;
    private MediaPlayer mPlayer;
    private Vibrator mVibrator;
    private ImageView mAlarmRingingClock;
    private UUID mAlarmId;
    private boolean mShowClockOnDragEnd;
    private ObjectAnimator mAnimateClock;
    private Alarm mAlarm;

    public interface RingingResultListener {
        void onRingingSnooze();
        void onRingingDismiss();
    }

    public static AlarmRingingFragment newInstance(String alarmId) {
        AlarmRingingFragment fragment = new AlarmRingingFragment();
        Bundle bundle = new Bundle(1);
        bundle.putString(ARGS_ALARM_ID, alarmId);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Logger.init(getActivity());
        Bundle args = getArguments();
        mAlarmId = UUID.fromString(args.getString(ARGS_ALARM_ID));
        mAlarm = AlarmList.get(getContext()).getAlarm(mAlarmId);

        View view = inflater.inflate(R.layout.fragment_alarm_ringing, container, false);

        if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN) {
            TextView timeField = (TextView) view.findViewById(R.id.alarm_ringing_time);
            timeField.setText(AlarmUtils.getUserTimeString(getContext(), mAlarm.getTimeHour(), mAlarm.getTimeMinute()));
        }

        TextView dateField = (TextView) view.findViewById(R.id.alarm_ringing_date);
        dateField.setText(AlarmUtils.getFullDateStringForNow());

        String name = mAlarm.getTitle();
        if (name == null || name.isEmpty()) {
            name = getString(R.string.alarm_ringing_default_text);
        }
        TextView titleField = (TextView) view.findViewById(R.id.alarm_ringing_title);
        titleField.setText(name);

        ImageView dismissButton = (ImageView) view.findViewById(R.id.alarm_ringing_dismiss);
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

        ImageView snoozeButton = (ImageView) view.findViewById(R.id.alarm_ringing_snooze);
        snoozeButton.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                switch (event.getAction()) {
                    case DragEvent.ACTION_DROP:
                        mCallback.onRingingSnooze();
                        break;
                    default:
                        break;
                }
                return true;
            }
        });

        mAlarmRingingClock = (ImageView) view.findViewById(R.id.alarm_ringing_clock);
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

        initializeClockAnimation();
        initializeMediaPlayer();

        Loggable.AppAction appAction = new Loggable.AppAction(Loggable.Key.APP_ALARM_RINGING);

        appAction.putJSON(mAlarm.toJSON());
        Logger.track(appAction);

        return view;
    }

    private void dismissAlarm() {
        mShowClockOnDragEnd = false;

        if (mAlarm.isOneShot()) {
            mAlarm.setIsEnabled(false);
            AlarmList.get(getContext()).updateAlarm(mAlarm);
        }

        Loggable.UserAction userAction = new Loggable.UserAction(Loggable.Key.ACTION_ALARM_DISMISS);
        Alarm alarm = AlarmList.get(getContext()).getAlarm(mAlarmId);
        userAction.putJSON(alarm.toJSON());
        Logger.track(userAction);

        mCallback.onRingingDismiss();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallback = (RingingResultListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, "Entered onResume!");

        playAlarmSound();
        vibrateDeviceIfDesired();
        mShowClockOnDragEnd = true;
        mAlarmRingingClock.setVisibility(View.VISIBLE);
        mAnimateClock.start();

        Util.registerCrashReport(getActivity());
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d(TAG, "Entered onPause!");

        cancelAlarmSound();
        cancelVibration();
        mAnimateClock.cancel();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "Entered onDestroy!");

        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }

    private void vibrateDeviceIfDesired() {
        if (mAlarm.shouldVibrate()) {
            mVibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
            // Start immediately
            // Vibrate for 200 milliseconds
            // Sleep for 500 milliseconds
            long[] vibrationPattern = {0, 200, 500};
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

    private void initializeMediaPlayer() {
        try {
            Uri toneUri = mAlarm.getAlarmTone();
            if (toneUri != null) {
                mPlayer = new MediaPlayer();
                mPlayer.setDataSource(getActivity(), toneUri);
                mPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                mPlayer.setLooping(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Logger.trackException(e);
        }
    }

    private void playAlarmSound() {
        try {
            if (mPlayer != null && !mPlayer.isPlaying()) {
                mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mp.start();
                    }
                });
                mPlayer.prepareAsync();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Logger.trackException(e);
        }
    }

    private void cancelAlarmSound() {
        if (mPlayer != null && mPlayer.isPlaying())
        {
            mPlayer.stop();
        }
    }

    private void initializeClockAnimation() {
        mAnimateClock = ObjectAnimator.ofFloat(mAlarmRingingClock, "translationY", -35f, 0f);
        mAnimateClock.setDuration(CLOCK_ANIMATION_DURATION);
        mAnimateClock.setInterpolator(new BounceInterpolator());
        mAnimateClock.setRepeatCount(ValueAnimator.INFINITE);
    }
}
