package com.microsoft.mimicker.mimics;

import android.util.Log;
import android.view.View;

public class MimicCoordinator implements IMimicMediator {
    private static String TAG = "MimicCoordinator";

    MimicStateBanner mMimicStateBanner;
    CountDownTimerView mCountDownTimer;
    ProgressButton mProgressButton;
    IMimicImplementation mMimic;
    boolean mMimicStopped;

    public void start(){
        Log.d(TAG, "Entered start!");
        mMimicStopped = false;
        mCountDownTimer.start();
    }

    public void stop() {
        Log.d(TAG, "Entered stop!");
        mMimicStopped = true;
        mMimic.stopService();
        mProgressButton.setReady();
    }

    public boolean hasStopped() {
        return mMimicStopped;
    }

    public void onMimicSuccess(String successMessage) {
        Log.d(TAG, "Entered onMimicSuccess!");
        if (!mMimicStopped) {
            mCountDownTimer.stop();
            mMimicStateBanner.success(successMessage, new MimicStateBanner.Command() {
                @Override
                public void execute() {
                    Log.d(TAG, "Entered onMimicSuccess callback!");
                    if (!mMimicStopped) {
                        mMimic.onSucceeded();
                    }
                }
            });
        }
    }

    public void onMimicFailureWithRetry(String failureMessage) {
        Log.d(TAG, "Entered onMimicFailureWithRetry!");
        if (!mMimicStopped && !mCountDownTimer.hasExpired()) {
            mCountDownTimer.pause();
            mMimicStateBanner.failure(failureMessage, new MimicStateBanner.Command() {
                @Override
                public void execute() {
                    Log.d(TAG, "Entered onMimicFailureWithRetry callback!");
                    if (!mMimicStopped) {
                        mCountDownTimer.resume();
                        mProgressButton.setReady();
                    }
                }
            });
        }
    }

    public void onMimicFailure(String failureMessage) {
        Log.d(TAG, "Entered onMimicFailure!");
        mCountDownTimer.stop();
        mProgressButton.setClickable(false);
        mMimicStateBanner.failure(failureMessage, new MimicStateBanner.Command() {
            @Override
            public void execute() {
                Log.d(TAG, "Entered onMimicFailure callback!");
                mMimic.onFailed();
            }
        });
    }

    public void registerStateBanner(MimicStateBanner mimicStateBanner) {
        mMimicStateBanner = mimicStateBanner;
    }

    public void registerCountDownTimer(CountDownTimerView countDownTimer, int timeout) {
        mCountDownTimer = countDownTimer;
        mCountDownTimer.init(timeout, new CountDownTimerView.Command() {
            @Override
            public void execute() {
                Log.d(TAG, "Countdown timer expired!");
                mMimic.stopService();
                mMimic.onCountDownTimerExpired();
            }
        });
    }

    public void registerProgressButton(ProgressButton progressButton) {
        mProgressButton = progressButton;
        mProgressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mProgressButton.isReady()) {
                    mMimic.startService();
                    mProgressButton.waiting();
                } else {
                    mMimic.stopService();
                }
            }
        });
        progressButton.setReady();
    }

    public void registerMimic(IMimicImplementation mimic) {
        mMimic = mimic;
        mMimic.initializeService();
    }
}
