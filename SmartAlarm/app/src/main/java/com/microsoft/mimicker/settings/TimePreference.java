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

package com.microsoft.mimicker.settings;

import android.content.Context;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.widget.TextView;

import com.microsoft.mimicker.R;
import com.microsoft.mimicker.utilities.DateTimeUtilities;

public class TimePreference extends DialogPreference {

    private TextView mTimeLabel;
    private int mHour;
    private int mMinute;
    private boolean mChanged;

    public TimePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.preference_time);
        setDialogLayoutResource(R.layout.preference_timedialog);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        mTimeLabel = (TextView) holder.findViewById(R.id.time_label);
        setTimeLabel();
    }

    public void setTime(int hour, int minute) {
        mHour = hour;
        mMinute = minute;
    }

    public int getMinute() {
        return mMinute;
    }

    public int getHour() {
        return mHour;
    }

    private void setTimeLabel() {
        mTimeLabel.setText(DateTimeUtilities.getUserTimeString(getContext(), mHour, mMinute));
    }

    public boolean hasChanged() {
        return mChanged;
    }

    public void setChanged(boolean changed) {
        mChanged = changed;
        setTimeLabel();
    }
}
