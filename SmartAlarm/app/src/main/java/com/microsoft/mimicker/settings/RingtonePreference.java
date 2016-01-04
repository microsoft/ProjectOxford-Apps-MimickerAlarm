package com.microsoft.mimicker.settings;

import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v7.preference.Preference;
import android.util.AttributeSet;

import com.microsoft.mimicker.R;
import com.microsoft.mimicker.utilities.Util;

public class RingtonePreference extends Preference {
    public static final int RINGTONE_PICKER_REQUEST = 1000;
    private boolean mChanged;
    private Uri mRingtone;
    private Fragment mParent;

    public RingtonePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public boolean hasChanged() {
        return mChanged;
    }

    public void setChanged(boolean changed) {
        mChanged = changed;
    }

    public void setParent(Fragment parent) {
        mParent = parent;
    }

    @Override
    protected void onClick() {
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        onPrepareRingtonePickerIntent(intent);
        mParent.startActivityForResult(intent, RINGTONE_PICKER_REQUEST);
    }

    public Uri getRingtone() {
        return mRingtone;
    }

    public void setRingtone(Uri ringtone) {
        mRingtone = ringtone;
        if (mRingtone == null) {
            setSummary(getContext().getString(R.string.pref_no_ringtone));
        }
        else if (mRingtone.toString().compareToIgnoreCase(Util.defaultRingtone().toString()) == 0) {
            setSummary(getContext().getString(R.string.default_ringtone_name));
        }
        else {
            setSummary(RingtoneManager.getRingtone(getContext(), mRingtone).getTitle(getContext()));
        }
    }

    public void handleRingtonePickerResult(Intent data) {
        if (data != null) {
            Uri ringtone = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (ringtone == null) {
                if (getRingtone() != null) {
                    setRingtone(null);
                    setChanged(true);
                }
            } else if (getRingtone() == null ||
                    getRingtone().toString().compareToIgnoreCase(ringtone.toString()) != 0) {
                setRingtone(ringtone);
                setChanged(true);
            }
        }
    }

    private void onPrepareRingtonePickerIntent(Intent ringtonePickerIntent) {
        ringtonePickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, getRingtone());
        ringtonePickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
        ringtonePickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, Util.defaultRingtone());
        ringtonePickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
        ringtonePickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL);
        ringtonePickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getTitle());
    }
}
