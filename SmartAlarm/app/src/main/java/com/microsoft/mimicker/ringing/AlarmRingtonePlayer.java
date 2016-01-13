package com.microsoft.mimicker.ringing;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;

import com.microsoft.mimicker.utilities.Logger;

public class AlarmRingtonePlayer {
    private MediaPlayer mPlayer;
    private Context mContext;

    public AlarmRingtonePlayer(Context context) {
        mContext = context;
    }

    public void initialize() {
        try {
            mPlayer = new MediaPlayer();
            mPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
            mPlayer.setLooping(true);
        } catch (Exception e) {
            Logger.trackException(e);
        }
    }

    public void cleanup() {
        mPlayer.release();
        mPlayer = null;
    }

    public void play(Uri toneUri) {
        try {
            if (mPlayer != null && !mPlayer.isPlaying()) {
                mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mp.start();
                    }
                });
                mPlayer.setDataSource(mContext, toneUri);
                mPlayer.prepareAsync();
            }
        } catch (Exception e) {
            Logger.trackException(e);
        }
    }

    public void stop() {
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.stop();
            mPlayer.reset();
        }
    }
}
