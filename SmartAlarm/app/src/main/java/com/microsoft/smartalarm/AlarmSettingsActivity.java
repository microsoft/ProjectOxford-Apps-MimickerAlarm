package com.microsoft.smartalarm;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.SwitchPreferenceCompat;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class AlarmSettingsActivity extends AppCompatActivity {

    private static final String ARGS_ALARM_ID = "alarm_id";
    private static final String EDIT_MODE = "edit_mode";
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

        final UUID alarmId = (UUID) getIntent()
                .getSerializableExtra(ARGS_ALARM_ID);

        mFragment = AlarmSettingsFragment.newInstance(alarmId.toString());
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, mFragment)
                .commit();

        setTitle(getString(R.string.pref_title_edit));
    }

    public static class AlarmSettingsFragment extends PreferenceFragmentCompat {

        private static final String PREFERENCE_DIALOG_FRAGMENT_CLASS = "android.support.v7.preference.PreferenceFragment.DIALOG";

        private boolean mEditMode = true;

        private UUID mAlarmId;
        private Alarm mAlarm;
        private TimePreference mTimePreference;
        private RepeatingDaysPreference mRepeatingDaysPreference;
        private NamePreference mNamePreference;
        private GamesPreference mGamesPreference;
        private RingtonePreference mRingtonePreference;
        private SwitchPreferenceCompat mVibratePreference;
        private ButtonsPreference mButtonsPreference;

        public static AlarmSettingsFragment newInstance(String alarmId) {
            AlarmSettingsFragment fragment = new AlarmSettingsFragment();
            Bundle bundle = new Bundle(1);
            bundle.putString(ARGS_ALARM_ID, alarmId);
            fragment.setArguments(bundle);
            return fragment;
        }

        @Override
        public void onCreatePreferences(Bundle bundle, String s) {
            addPreferencesFromResource(R.xml.pref_alarm);

            Bundle args = getArguments();
            mAlarmId = UUID.fromString(args.getString(ARGS_ALARM_ID));
            mAlarm = AlarmList.get(getContext()).getAlarm(mAlarmId);

            mTimePreference = (TimePreference) findPreference(getString(R.string.pref_time_key));
            mTimePreference.setTime(mAlarm.getTimeHour(), mAlarm.getTimeMinute());

            mRepeatingDaysPreference = (RepeatingDaysPreference) findPreference(getString(R.string.pref_repeating_days_key));
            CharSequence[] menuItems = mRepeatingDaysPreference.getEntryValues();
            Set<String> values = new HashSet<>();
            for (int i = 0; i < 7; ++i) {
                if (mAlarm.getRepeatingDay(i)) {
                    values.add(menuItems[i].toString());
                }
            }
            mRepeatingDaysPreference.setValues(values);
            mRepeatingDaysPreference.setSummaryValues(values);

            mNamePreference = (NamePreference) findPreference(getString(R.string.pref_name_key));
            mNamePreference.setAlarmName(mAlarm.getTitle());

            mGamesPreference = (GamesPreference) findPreference(getString(R.string.pref_games_key));
            mGamesPreference.setTongueTwisterEnabled(mAlarm.isTongueTwisterEnabled());
            mGamesPreference.setColorCollectorEnabled(mAlarm.isColorCollectorEnabled());
            mGamesPreference.setExpressYourselfEnabled(mAlarm.isExpressYourselfEnabled());
            mGamesPreference.setInitialValues();

            mRingtonePreference = (RingtonePreference) findPreference(getString(R.string.pref_ringtone_key));
            mRingtonePreference.setRingtone(mAlarm.getAlarmTone());

            mVibratePreference = (SwitchPreferenceCompat) findPreference(getString(R.string.pref_vibrate_key));
            mVibratePreference.setChecked(mAlarm.shouldVibrate());

            mButtonsPreference = (ButtonsPreference) findPreference(getString(R.string.pref_buttons_key));
            mButtonsPreference.setLeftButtonText(getResources().getString(R.string.pref_button_cancel));
            mButtonsPreference.setRightButtonText(getResources().getString(R.string.pref_button_save));

            mButtonsPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    handleSettingsExit((boolean) o);
                    return true;
                }
            });
        }

        @Override
        public void onDisplayPreferenceDialog(Preference preference) {
            if (preference instanceof RepeatingDaysPreference) {
                DialogFragment dialogFragment = MultiSelectListPreferenceDialogFragmentCompat.newInstance(preference);
                dialogFragment.setTargetFragment(this, 0);
                dialogFragment.show(getFragmentManager(), PREFERENCE_DIALOG_FRAGMENT_CLASS);
            } else if (preference instanceof TimePreference) {
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

        private void handleSettingsExit(boolean persistSettings) {
            boolean dirty = false;

            if (mTimePreference.isDirty()) {
                mAlarm.setTimeHour(mTimePreference.getHour());
                mAlarm.setTimeMinute(mTimePreference.getMinute());
                dirty = true;
            }

            if (mRepeatingDaysPreference.isDirty()) {
                boolean[] repeatingDays = mRepeatingDaysPreference.getRepeatingDays();
                for (int i = 0; i < repeatingDays.length; i++) {
                    mAlarm.setRepeatingDay(i, repeatingDays[i]);
                }
                dirty = true;
            }

            if (mAlarm.getTitle().compareTo(mNamePreference.getText()) != 0) {
                mAlarm.setTitle(mNamePreference.getText());
                dirty = true;
            }

            if (mGamesPreference.isDirty()) {
                mAlarm.setTongueTwisterEnabled(mGamesPreference.isTongueTwisterEnabled());
                mAlarm.setColorCollectorEnabled(mGamesPreference.isColorCollectorEnabled());
                mAlarm.setExpressYourselfEnabled(mGamesPreference.isExpressYourselfEnabled());
                dirty = true;
            }

            if (mRingtonePreference.isDirty()) {
                mAlarm.setAlarmTone(mRingtonePreference.getRingtone());
                dirty = true;
            }

            if (mAlarm.shouldVibrate() != mVibratePreference.isChecked()) {
                mAlarm.setVibrate(mVibratePreference.isChecked());
                dirty = true;
            }

            if (persistSettings && dirty) {
                AlarmManagerHelper.cancelAlarms(getContext());
                AlarmList.get(getActivity()).updateAlarm(mAlarm);
                AlarmManagerHelper.setAlarms(getContext());
            }

            getActivity().finish();
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
