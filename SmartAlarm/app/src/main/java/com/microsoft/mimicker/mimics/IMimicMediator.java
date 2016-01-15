package com.microsoft.mimicker.mimics;

/**
 * This interface is implemented by the MimicStateManager class to control the state of common
 * Mimic UI controls
 */

enum MimicButtonBehavior {
    AUDIO,
    CAMERA
}

public interface IMimicMediator {
    void start();
    void stop();
    boolean isMimicRunning();

    void onMimicSuccess(String successMessage);
    void onMimicFailureWithRetry(String failureMessage);
    void onMimicFailure(String failureMessage);

    void registerStateBanner(MimicStateBanner mimicStateBanner);
    void registerCountDownTimer(CountDownTimerView countDownTimerView, int timeout);
    void registerProgressButton(ProgressButton progressButton, MimicButtonBehavior buttonBehavior);
    void registerMimic(IMimicImplementation mimic);
}