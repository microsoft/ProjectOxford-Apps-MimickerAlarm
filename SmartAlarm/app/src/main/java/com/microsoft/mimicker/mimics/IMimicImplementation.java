package com.microsoft.mimicker.mimics;

public interface IMimicImplementation {
    void initializeService();

    void startService();
    void stopService();

    void onCountDownTimerExpired();
    void onSucceeded();
    void onFailed();
}
