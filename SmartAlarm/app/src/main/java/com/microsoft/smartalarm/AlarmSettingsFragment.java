package com.microsoft.smartalarm;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.UUID;

public class AlarmSettingsFragment extends PreferenceFragmentCompat {
    AlarmSettingsListener mCallback;

    public interface AlarmSettingsListener {
        void onSettingsSaveOrIgnoreChanges();
        void onSettingsDeleteOrNewCancel();
    }

    public final String TAG = this.getClass().getSimpleName();
    public static final String SETTINGS_FRAGMENT_TAG = "settings_fragment";
    private static final String ARGS_ALARM_ID = "alarm_id";
    private static final String PREFERENCE_DIALOG_FRAGMENT_CLASS = "android.support.v7.preference.PreferenceFragment.DIALOG";

    private UUID mAlarmId;
    private Alarm mAlarm;
    private TimePreference mTimePreference;
    private RepeatingDaysPreference mRepeatingDaysPreference;
    private NamePreference mNamePreference;
    private GamesPreference mGamesPreference;
    private RingtonePreference mRingtonePreference;
    private VibratePreference mVibratePreference;
    private ButtonsPreference mButtonsPreference;

    public static AlarmSettingsFragment newInstance(String alarmId) {
        AlarmSettingsFragment fragment = new AlarmSettingsFragment();
        Bundle bundle = new Bundle(1);
        bundle.putString(ARGS_ALARM_ID, alarmId);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallback = (AlarmSettingsListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
        Logger.flush();
    }

    @Override
    public void onCreatePreferences(Bundle bundle, final String s) {
        addPreferencesFromResource(R.xml.pref_alarm);

        Bundle args = getArguments();
        mAlarmId = UUID.fromString(args.getString(ARGS_ALARM_ID));
        mAlarm = AlarmList.get(getContext()).getAlarm(mAlarmId);

        initializeTimePreference();
        initializeRepeatingDaysPreference();
        initializeNamePreference();
        initializeGamesPreference();
        initializeRingtonePreference();
        initializeVibratePreference();
        initializeButtons();
    }

    @Override
    public RecyclerView onCreateRecyclerView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        LinearLayout rootLayout = (LinearLayout) parent.getParent();
        AppBarLayout appBarLayout = (AppBarLayout) LayoutInflater.from(getContext()).inflate(R.layout.settings_toolbar, rootLayout, false);
        rootLayout.addView(appBarLayout, 0); // insert at top
        Toolbar bar = (Toolbar) appBarLayout.findViewById(R.id.settings_toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(bar);
        bar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCancel();
            }
        });

        Loggable.UserAction userAction;
        if (mAlarm.isNew()) {
            userAction = new Loggable.UserAction(Loggable.Key.ACTION_ALARM_CREATE);
            bar.setTitle(R.string.pref_title_new);
        } else {
            userAction = new Loggable.UserAction(Loggable.Key.ACTION_ALARM_EDIT);
            bar.setTitle(R.string.pref_title_edit);
        }
        Logger.track(userAction);

        return super.onCreateRecyclerView(inflater, parent, savedInstanceState);
    }

    private void initializeTimePreference() {
        mTimePreference = (TimePreference) findPreference(getString(R.string.pref_time_key));
        mTimePreference.setTime(mAlarm.getTimeHour(), mAlarm.getTimeMinute());
    }

    private void initializeRepeatingDaysPreference() {
        mRepeatingDaysPreference = (RepeatingDaysPreference) findPreference(getString(R.string.pref_repeating_days_key));
        for (int i = 0; i < 7; ++i) {
            if (mAlarm.getRepeatingDay(i)) {
                mRepeatingDaysPreference.setRepeatingDay(i, true);
            }
        }
    }

    private void initializeNamePreference() {
        mNamePreference = (NamePreference) findPreference(getString(R.string.pref_name_key));
        mNamePreference.setAlarmName(mAlarm.getTitle());
    }

    private void initializeGamesPreference() {
        mGamesPreference = (GamesPreference) findPreference(getString(R.string.pref_games_key));
        mGamesPreference.setTongueTwisterEnabled(mAlarm.isTongueTwisterEnabled());
        mGamesPreference.setColorCollectorEnabled(mAlarm.isColorCollectorEnabled());
        mGamesPreference.setExpressYourselfEnabled(mAlarm.isExpressYourselfEnabled());
        mGamesPreference.setInitialValues();
    }

    private void initializeRingtonePreference() {
        mRingtonePreference = (RingtonePreference) findPreference(getString(R.string.pref_ringtone_key));
        mRingtonePreference.setRingtone(mAlarm.getAlarmTone());
        mRingtonePreference.setParent(this);
    }

    private void initializeVibratePreference() {
        mVibratePreference = (VibratePreference) findPreference(getString(R.string.pref_vibrate_key));
        mVibratePreference.setInitialValue(mAlarm.shouldVibrate());
    }

    private void initializeButtons() {
        mButtonsPreference = (ButtonsPreference) findPreference(getString(R.string.pref_buttons_key));
        int resId = mAlarm.isNew() ? android.R.string.cancel : R.string.pref_button_delete;
        mButtonsPreference.setLeftButtonText(getResources().getString(resId));
        mButtonsPreference.setRightButtonText(getResources().getString(R.string.pref_button_save));
        mButtonsPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                boolean rightButtonPressed = (boolean) o;
                if (rightButtonPressed) {
                    // Save button was pressed
                    saveSettingsAndExit();
                } else {
                    // Cancel (when new) or Delete button was pressed
                    deleteSettingsAndExit();
                }
                return true;
            }
        });
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        if (preference instanceof TimePreference) {
            DialogFragment dialogFragment = TimePreferenceDialogFragmentCompat.newInstance(preference);
            dialogFragment.setTargetFragment(this, 0);
            dialogFragment.show(getFragmentManager(), PREFERENCE_DIALOG_FRAGMENT_CLASS);
        } else if (preference instanceof NamePreference) {
            DialogFragment dialogFragment = NamePreferenceDialogFragmentCompat.newInstance(preference);
            dialogFragment.setTargetFragment(this, 0);
            dialogFragment.show(getFragmentManager(), PREFERENCE_DIALOG_FRAGMENT_CLASS);
        } else if (preference instanceof GamesPreference) {
            DialogFragment dialogFragment = MultiSelectListPreferenceDialogFragmentCompat.newInstance(preference);
            dialogFragment.setTargetFragment(this, 0);
            dialogFragment.show(getFragmentManager(), PREFERENCE_DIALOG_FRAGMENT_CLASS);
        } else super.onDisplayPreferenceDialog(preference);
    }

    public void onCancel() {
        if (haveSettingsChanged() || mAlarm.isNew()) {
            new AlertDialog.Builder(getContext())
                    .setMessage(R.string.pref_dialog_save_changes_message)
                    .setPositiveButton(R.string.pref_dialog_save_changes_positive_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            saveSettingsAndExit();
                        }
                    })
                    .setNegativeButton(R.string.pref_dialog_save_changes_negative_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (mAlarm.isNew()) {
                                deleteSettingsAndExit();
                            } else {
                                discardSettingsAndExit();
                            }
                        }
                    })
                    .show();
        } else {
            discardSettingsAndExit();
        }
    }

    private void saveSettingsAndExit() {
        Loggable.UserAction userAction = new Loggable.UserAction(Loggable.Key.ACTION_ALARM_SAVE);
        userAction.putJSON(mAlarm.toJSON());
        Logger.track(userAction);

        populateUpdatedSettings();
        mAlarm.setNew(false);

        if (mAlarm.isEnabled()) {
            AlarmScheduler.cancelAlarm(getContext(), mAlarm);
        } else {
            mAlarm.setIsEnabled(true);
        }

        AlarmList.get(getActivity()).updateAlarm(mAlarm);
        AlarmScheduler.scheduleAlarm(getContext(), mAlarm);

        mCallback.onSettingsSaveOrIgnoreChanges();
    }

    private void deleteSettingsAndExit() {
        Loggable.UserAction userAction = new Loggable.UserAction(Loggable.Key.ACTION_ALARM_DELETE);
        userAction.putJSON(mAlarm.toJSON());
        Logger.track(userAction);

        if (mAlarm.isEnabled()) {
            AlarmScheduler.cancelAlarm(getContext(), mAlarm);
        }
        AlarmList.get(getActivity()).deleteAlarm(mAlarm);

        mCallback.onSettingsDeleteOrNewCancel();
    }

    private void discardSettingsAndExit() {
        Loggable.UserAction userAction = new Loggable.UserAction(Loggable.Key.ACTION_ALARM_SAVE_DISCARD);
        userAction.putJSON(mAlarm.toJSON());
        Logger.track(userAction);

        mCallback.onSettingsSaveOrIgnoreChanges();
    }

    private boolean haveSettingsChanged() {
        return mTimePreference.hasChanged() ||
                mRepeatingDaysPreference.hasChanged() ||
                mNamePreference.hasChanged() ||
                mGamesPreference.hasChanged() ||
                mRingtonePreference.hasChanged() ||
                mVibratePreference.hasChanged();
    }

    private void populateUpdatedSettings() {
        updateTimeSetting();
        updateRepeatingDaysSetting();
        updateNameSetting();
        updateGamesSetting();
        updateRingtoneSetting();
        updateVibrateSetting();
    }

    private void updateVibrateSetting() {
        if (mVibratePreference.hasChanged()) {
            mAlarm.setVibrate(mVibratePreference.isChecked());
        }
    }

    private void updateRingtoneSetting() {
        if (mRingtonePreference.hasChanged()) {
            mAlarm.setAlarmTone(mRingtonePreference.getRingtone());
        }
    }

    private void updateGamesSetting() {
        if (mGamesPreference.hasChanged()) {
            mAlarm.setTongueTwisterEnabled(mGamesPreference.isTongueTwisterEnabled());
            mAlarm.setColorCollectorEnabled(mGamesPreference.isColorCollectorEnabled());
            mAlarm.setExpressYourselfEnabled(mGamesPreference.isExpressYourselfEnabled());
        }
    }

    private void updateNameSetting() {
        if (mNamePreference.hasChanged()) {
            mAlarm.setTitle(mNamePreference.getText());
        }
    }

    private void updateRepeatingDaysSetting() {
        if (mRepeatingDaysPreference.hasChanged()) {
            boolean[] repeatingDays = mRepeatingDaysPreference.getRepeatingDays();
            for (int i = 0; i < repeatingDays.length; i++) {
                mAlarm.setRepeatingDay(i, repeatingDays[i]);
            }
        }
    }

    private void updateTimeSetting() {
        if (mTimePreference.hasChanged()) {
            mAlarm.setTimeHour(mTimePreference.getHour());
            mAlarm.setTimeMinute(mTimePreference.getMinute());
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == RingtonePreference.RINGTONE_PICKER_REQUEST) {
                mRingtonePreference.handleRingtonePickerResult(data);
            }
        }
    }
}

