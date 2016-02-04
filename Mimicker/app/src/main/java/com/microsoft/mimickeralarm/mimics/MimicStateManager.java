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

package com.microsoft.mimickeralarm.mimics;

import android.util.Log;
import android.view.View;

import java.lang.ref.WeakReference;

/**
 *  This class coordinates Mimic game state between the common UI controls:
 *      The countdown timer across the top of the screen
 *      The progress button which is used to capture images or audio
 *      The banner which animates across the screen giving user feedback
 *
 *  This class should be instantiated in a Mimic game fragment. The fragment should
 *  implement IMimicImplementation and register itself and its controls with the class. This
 *  class implements the IMimicMediator interface
 *
 *  The fragment should call into this class in the following cases:
 *      It should register the appropriate controls and itself
 *      The fragment onStart and onStop implementations should call start and stop
 *      All game failure/success cases must call into this class
 *
 *  The fragment can optionally call the isMimicRunning method to determine whether to do any
 *  further processing.
 */
public class MimicStateManager implements IMimicMediator {
    private static String TAG = "MimicStateManager";

    MimicStateBanner mMimicStateBanner;
    CountDownTimerView mCountDownTimer;
    ProgressButton mProgressButton;
    MimicButtonBehavior mButtonBehavior;
    WeakReference<IMimicImplementation> mMimicRef;
    boolean mMimicRunning;

    // Should be called from Fragment::onStart, which is when it becomes visible to the user
    public void start(){
        Log.d(TAG, "Entered start!");
        mMimicRunning = true;
        mCountDownTimer.start();
        IMimicImplementation mimic = mMimicRef.get();
        if (mimic != null) {
            mimic.initializeCapture();
        }
    }

    // Should be called from Fragment::onStop, when the fragment is invisible
    public void stop() {
        Log.d(TAG, "Entered stop!");
        mMimicRunning = false;
        IMimicImplementation mimic = mMimicRef.get();
        if (mimic != null) {
            mimic.stopCapture();
        }
        mProgressButton.setReady();
    }

    public boolean isMimicRunning() {
        return mMimicRunning;
    }

    public void onMimicSuccess(String successMessage) {
        Log.d(TAG, "Entered onMimicSuccess!");
        if (isMimicRunning()) {
            handleButtonState();
            mCountDownTimer.stop();
            mMimicStateBanner.success(successMessage, new MimicStateBanner.Command() {
                @Override
                public void execute() {
                    Log.d(TAG, "Entered onMimicSuccess callback!");
                    if (isMimicRunning()) {
                        IMimicImplementation mimic = mMimicRef.get();
                        if (mimic != null) {
                            mimic.onSucceeded();
                        }
                    }
                }
            });
        }
    }

    public void onMimicFailureWithRetry(String failureMessage) {
        Log.d(TAG, "Entered onMimicFailureWithRetry!");
        // If the countdown timer has just expired and has already registered a failure command,
        // then we should avoid changing state
        if (isMimicRunning() && !mCountDownTimer.hasExpired()) {
            mCountDownTimer.pause();
            mMimicStateBanner.failure(failureMessage, new MimicStateBanner.Command() {
                @Override
                public void execute() {
                    Log.d(TAG, "Entered onMimicFailureWithRetry callback!");
                    if (isMimicRunning()) {
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
                IMimicImplementation mimic = mMimicRef.get();
                if (mimic != null) {
                    mimic.onFailed();
                }
            }
        });
    }

    public void onMimicInternalError() {
        Log.d(TAG, "Entered onMimicInternalError!");
        handleButtonState();
        mCountDownTimer.stop();
        mProgressButton.setClickable(false);
        IMimicImplementation mimic = mMimicRef.get();
        if (mimic != null) {
            mimic.onInternalError();
        }
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
                if (isMimicRunning()) {
                    IMimicImplementation mimic = mMimicRef.get();
                    if (mimic != null) {
                        mimic.stopCapture();
                        mimic.onCountDownTimerExpired();
                    }
                }
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
                        IMimicImplementation mimic = mMimicRef.get();
                        if (mimic != null) {
                            mimic.startCapture();
                        }
                        mProgressButton.waiting();
                    } else {
                        IMimicImplementation mimic = mMimicRef.get();
                        if (mimic != null) {
                            mimic.stopCapture();
                        }
                    }
                }
            });
        } else if (mButtonBehavior == MimicButtonBehavior.CAMERA) {
            mProgressButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCountDownTimer.pause();
                    mProgressButton.loading();
                    IMimicImplementation mimic = mMimicRef.get();
                    if (mimic != null) {
                        mimic.startCapture();
                    }
                }
            });
        }
        mProgressButton.setReady();
    }

    public void registerMimic(IMimicImplementation mimic) {
        mMimicRef = new WeakReference<>(mimic);
    }

    private void handleButtonState() {
        if (mButtonBehavior == MimicButtonBehavior.CAMERA) {
            mProgressButton.stop();
        }
    }
}
