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

package com.microsoft.mimicker.globalsettings;

import android.content.Context;
import android.media.AudioManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.widget.SeekBar;

import com.microsoft.mimicker.R;

/**
 * This class is a custom preference to enable the display of a volume slider.  As the user slides
 * the SeekBar, the system alarm volume is changed.
 */
public class VolumeSliderPreference extends Preference
    implements SeekBar.OnSeekBarChangeListener {

    private SeekBar mVolumeLevel;
    private AudioManager mAudioManager;

    public VolumeSliderPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.preference_volumeslider);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
        int currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_ALARM);

        mVolumeLevel = (SeekBar) holder.findViewById(R.id.volume_level);
        mVolumeLevel.setMax(maxVolume);
        mVolumeLevel.setProgress(currentVolume);
        mVolumeLevel.setOnSeekBarChangeListener(this);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM,
                progress,
                AudioManager.FLAG_PLAY_SOUND);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    public void increaseVolume() {
        int current = mVolumeLevel.getProgress();
        if (current < mVolumeLevel.getMax()) {
            mVolumeLevel.setProgress(current + 1);
        }
    }

    public void decreaseVolume() {
        int current = mVolumeLevel.getProgress();
        if (current > 0) {
            mVolumeLevel.setProgress(current - 1);
        }
    }

}
