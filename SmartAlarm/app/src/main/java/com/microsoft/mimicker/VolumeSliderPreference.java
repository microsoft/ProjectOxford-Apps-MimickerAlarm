package com.microsoft.mimicker;

import android.content.Context;
import android.media.AudioManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.widget.SeekBar;

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
