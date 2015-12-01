package com.microsoft.smartalarm;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v7.preference.Preference;
import android.util.AttributeSet;

public class RingtonePreference extends Preference {
    private boolean mDirty;
    private Uri mRingtone;

    public static final int RINGTONE_PICKER_REQUEST = 1000;

    public RingtonePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public boolean isDirty() {
        return mDirty;
    }

    public void setDirty(boolean dirty) {
        mDirty = dirty;
    }

    @Override
    protected void onClick() {
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        onPrepareRingtonePickerIntent(intent);

        AlarmSettingsActivity  settingsActivity = findSettingsActivity(getContext());
        if (settingsActivity != null) {
            Fragment owningFragment = settingsActivity.getSettingsFragment();
            if (owningFragment != null) {
                owningFragment.startActivityForResult(intent, RINGTONE_PICKER_REQUEST);
            }
        }
    }

    public Uri getRingtone() {
        return mRingtone;
    }

    public void setRingtone(Uri ringtone) {
        mRingtone = ringtone;
        if (mRingtone == null) {
            setSummary(getContext().getString(R.string.pref_no_ringtone));
        } else {
            setSummary(RingtoneManager.getRingtone(getContext(), mRingtone).getTitle(getContext()));
        }
    }

    public void handleRingtonePickerResult(Intent data) {
        if (data != null) {
            Uri ringtone = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (ringtone == null) {
                if (getRingtone() != null) {
                    setRingtone(null);
                    setDirty(true);
                }
            } else if (getRingtone() == null ||
                    getRingtone().toString().compareToIgnoreCase(ringtone.toString()) != 0) {
                setRingtone(ringtone);
                setDirty(true);
            }
        }
    }

    private void onPrepareRingtonePickerIntent(Intent ringtonePickerIntent) {
        ringtonePickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, getRingtone());
        ringtonePickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
        ringtonePickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALL));
        ringtonePickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
        ringtonePickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL);
        ringtonePickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getTitle());
    }


    private static AlarmSettingsActivity findSettingsActivity(Context context) {
        if (context == null)
            return null;
        else if (context instanceof AlarmSettingsActivity)
            return (AlarmSettingsActivity)context;
        else if (context instanceof ContextWrapper)
            return findSettingsActivity(((ContextWrapper) context).getBaseContext());

        return null;
    }
}
