package com.microsoft.mimicker.mimics;

/**
 * Mimic fragment classes should implement this interface to allow the MimicStateManager class
 * to control audio/image capture and also to allow the class to take the appropriate action
 * based on the following state changes: Mimic timeout, failure and success
 */
public interface IMimicImplementation {
    void initializeCapture();
    void startCapture();
    void stopCapture();

    void onCountDownTimerExpired();
    void onSucceeded();
    void onFailed();
}
