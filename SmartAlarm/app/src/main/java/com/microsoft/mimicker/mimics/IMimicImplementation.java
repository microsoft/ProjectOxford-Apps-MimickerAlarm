package com.microsoft.mimicker.mimics;

/**
 * Mimic fragment classes should implement this interface to allow the MimicCoordinator class
 * to control audio/image capture and also to allow the class to take the appropriate action
 * based on the following state changes: Mimic timeout, Failure and Success
 */
public interface IMimicImplementation {
    void initializeCapture();
    void startCapture();
    void stopCapture();

    void onCountDownTimerExpired();
    void onSucceeded();
    void onFailed();
}
