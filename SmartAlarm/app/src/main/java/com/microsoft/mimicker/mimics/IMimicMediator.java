package com.microsoft.mimicker.mimics;

public interface IMimicMediator {
    void start();
    void stop();
    boolean hasStopped();

    void onMimicSuccess(String successMessage);
    void onMimicFailureWithRetry(String failureMessage);
    void onMimicFailure(String failureMessage);

    void registerStateBanner(MimicStateBanner mimicStateBanner);
    void registerCountDownTimer(CountDownTimerView countDownTimerView, int timeout);
    void registerProgressButton(ProgressButton progressButton);
    void registerMimic(IMimicImplementation mimic);
}