package com.microsoft.smartalarm;

import java.util.Date;
import java.util.UUID;

public class Alarm {
    UUID    mId;
    String  mTitle;
    String  mTime; // TODO Replace with appropriate type
    String  mDaysofWeek; // TODO For visual effect now only
    boolean mIsEnabled;

    public Alarm () {
        mId = UUID.randomUUID();
    }
    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public boolean isEnabled() {
        return mIsEnabled;
    }

    public void setIsEnabled(boolean isEnabled) {
        mIsEnabled = isEnabled;
    }

    public UUID getId() {
        return mId;
    }
}
