package com.microsoft.mimicker.mimics;

import android.util.Log;
import android.view.View;

/**
 *  This class coordinates Mimic game state between the common UI controls:
 *      The countdown timer across the top of the screen
 *      The progress button which is used to capture images or audio
 *      The banner which animates across the screen giving user feedback
 *
 *  This class should be instantiated in a Mimic game fragment. The fragment should
 *  implement IMimicImplementation and register itself and the its controls with the class. This
 *  class implements the IMimicMediator interface
 *
 *  The fragment should call into this class in the following cases:
 *      Registration of the controls and itself
 *      The fragment onStart and onStop implementations should call start and stop
 *      All game failure/success cases must call into this class
 *
 *  The fragment can optionally called the hasStopped method to determine whether to do any
 *  further processing.
 */
public class MimicCoordinator implements IMimicMediator {
    private static String TAG = "MimicCoordinator";

    MimicStateBanner mMimicStateBanner;
    CountDownTimerView mCountDownTimer;
    ProgressButton mProgressButton;
    MimicButtonBehavior mButtonBehavior;
    IMimicImplementation mMimic;
    boolean mMimicStopped;

    // Should be called from Fragment::onStart, which is when it becomes visible to the user
    public void start(){
        Log.d(TAG, "Entered start!");
        mMimicStopped = false;
        mCountDownTimer.start();
        mMimic.initializeCapture();
    }

    // Should be called from Fragment::onStop, when the fragment is invisible
    public void stop() {
        Log.d(TAG, "Entered stop!");
        mMimicStopped = true;
        mMimic.stopCapture();
        mProgressButton.setReady();
    }

    public boolean hasStopped() {
        return mMimicStopped;
    }

    public void onMimicSuccess(String successMessage) {
        Log.d(TAG, "Entered onMimicSuccess!");
        if (!hasStopped()) {
            handleButtonState();
            mCountDownTimer.stop();
            mMimicStateBanner.success(successMessage, new MimicStateBanner.Command() {
                @Override
                public void execute() {
                    Log.d(TAG, "Entered onMimicSuccess callback!");
                    if (!hasStopped()) {
                        mMimic.onSucceeded();
                    }
                }
            });
        }
    }

    public void onMimicFailureWithRetry(String failureMessage) {
        Log.d(TAG, "Entered onMimicFailureWithRetry!");
        // If the countdown time has just expired and has already registered a failure command,
        // then we should avoid changing state
        if (!hasStopped() && !mCountDownTimer.hasExpired()) {
            mCountDownTimer.pause();
            mMimicStateBanner.failure(failureMessage, new MimicStateBanner.Command() {
                @Override
                public void execute() {
                    Log.d(TAG, "Entered onMimicFailureWithRetry callback!");
                    if (!hasStopped()) {
                        mCountDownTimer.resume();
                        mProgressButton.setReady();
                    }
                }
            });
        }
    }

    public void onMimicFailure(String failureMessage) {
        Log.d(TAG, "Entered onMimicFailure!");
        handleButtonState();
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
                mMimic.stopCapture();
                mMimic.onCountDownTimerExpired();
            }
        });
    }

    public void registerProgressButton(ProgressButton progressButton,
                                       MimicButtonBehavior buttonBehavior) {
        mProgressButton = progressButton;
        mButtonBehavior = buttonBehavior;

        if (mButtonBehavior == MimicButtonBehavior.AUDIO) {
            mProgressButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mProgressButton.isReady()) {
                        mMimic.startCapture();
                        mProgressButton.waiting();
                    } else {
                        mMimic.stopCapture();
                    }
                }
            });
        } else if (mButtonBehavior == MimicButtonBehavior.CAMERA) {
            mProgressButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCountDownTimer.pause();
                    mProgressButton.loading();
                    mMimic.startCapture();
                }
            });
        }
        mProgressButton.setReady();
    }

    public void registerMimic(IMimicImplementation mimic) {
        mMimic = mimic;
    }

    private void handleButtonState() {
        if (mButtonBehavior == MimicButtonBehavior.CAMERA) {
            mProgressButton.stop();
        }
    }
}
