package com.microsoft.smartalarm;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.MenuItem;

import java.util.UUID;

public class AlarmSettingsActivity extends AppCompatActivity {

    private static final String ARGS_ALARM_ID = "alarm_id";

    private Fragment mFragment;

    public static Intent newIntent(Context packageContext, UUID alarmId) {
        Intent intent = new Intent(packageContext, AlarmSettingsActivity.class);
        intent.putExtra(ARGS_ALARM_ID, alarmId);
        return intent;
    }

    public Fragment getSettingsFragment () {
        return mFragment;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.init(this);

        final UUID alarmId = (UUID) getIntent()
                .getSerializableExtra(ARGS_ALARM_ID);

        boolean newAlarm = AlarmList.get(this).getAlarm(alarmId).isNew();

        Loggable.UserAction userAction;
        if (newAlarm) {
            userAction = new Loggable.UserAction(Loggable.Key.ACTION_ALARM_CREATE);
        }
        else {
            userAction = new Loggable.UserAction(Loggable.Key.ACTION_ALARM_EDIT);
        }
        Logger.track(userAction);

        mFragment = AlarmSettingsFragment.newInstance(alarmId.toString());
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, mFragment)
                .commit();

        if (newAlarm) {
            setTitle(R.string.pref_title_new);
        } else {
            setTitle(R.string.pref_title_edit);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logger.flush();
    }

    @Override
    public void onBackPressed() {
        ((AlarmSettingsFragment)getSettingsFragment()).onCancel();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                ((AlarmSettingsFragment)getSettingsFragment()).onCancel();
                break;
        }
        return true;
    }

    public static class AlarmSettingsFragment extends PreferenceFragmentCompat {

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
                        if (haveSettingsChanged()) {
                            saveSettingsAndExit();
                        } else {
                            discardSettingsAndExit();
                        }
                    } else {
                        if (mAlarm.isNew()) {
                            // Cancel button was pressed
                            discardSettingsAndExit();
                        } else {
                            // Delete button was pressed
                            deleteSettingsAndExit();
                        }
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

        private void onCancel() {
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
            AlarmManagerHelper.cancelAlarms(getContext());
            populateUpdatedSettings();
            mAlarm.setNew(false);
            Loggable.UserAction userAction = new Loggable.UserAction(Loggable.Key.ACTION_ALARM_SAVE);
            userAction.putJSON(mAlarm.toJSON());
            Logger.track(userAction);
            AlarmList.get(getActivity()).updateAlarm(mAlarm);
            AlarmManagerHelper.setAlarms(getContext());
            getActivity().finish();
        }

        private void deleteSettingsAndExit() {
            Loggable.UserAction userAction = new Loggable.UserAction(Loggable.Key.ACTION_ALARM_DELETE);
            userAction.putJSON(mAlarm.toJSON());
            Logger.track(userAction);

            AlarmManagerHelper.cancelAlarms(getContext());
            AlarmList.get(getActivity()).deleteAlarm(mAlarm);
            AlarmManagerHelper.setAlarms(getContext());
            getActivity().finish();
        }

        private void discardSettingsAndExit() {
            Loggable.UserAction userAction = new Loggable.UserAction(Loggable.Key.ACTION_ALARM_SAVE_DISCARD);
            userAction.putJSON(mAlarm.toJSON());
            Logger.track(userAction);
            if (mAlarm.isNew()) {
                mAlarm.setNew(false);
                AlarmList.get(getActivity()).updateAlarm(mAlarm);
            }
            getActivity().finish();
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
}
