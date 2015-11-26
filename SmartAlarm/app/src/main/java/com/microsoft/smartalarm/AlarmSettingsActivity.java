package com.microsoft.smartalarm;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class AlarmSettingsActivity extends AppCompatActivity
    implements AlarmFragment.Callbacks {

    private static final String ARGS_ALARM_ID = "alarm_id";
    private static final String EDIT_MODE = "edit_mode";

    public static Intent newIntent(Context packageContext, UUID alarmId) {
        Intent intent = new Intent(packageContext, AlarmSettingsActivity.class);
        intent.putExtra(ARGS_ALARM_ID, alarmId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final UUID alarmId = (UUID) getIntent()
                .getSerializableExtra(ARGS_ALARM_ID);

        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, AlarmSettingsFragment.newInstance(alarmId.toString()))
                .commit();

    }

    @Override
    public void onAlarmUpdated(Alarm alarm) {

    }

    public static class AlarmSettingsFragment extends PreferenceFragmentCompat {

        private boolean mEditMode = true;

        private UUID mAlarmId;
        private Alarm mAlarm;
        private TimePreference mTimePreference;
        private RepeatingDaysPreference mRepeatingDaysPreference;
        private NamePreference mNamePreference;
        private GamesPreference mGamesPreference;
        private RingtonePreference mRingtonePreference;

        private ButtonsPreference mButtonsPreference;

        public static AlarmSettingsFragment newInstance(String alarmId) {
            AlarmSettingsFragment fragment = new AlarmSettingsFragment();
            Bundle bundle = new Bundle(1);
            bundle.putString(ARGS_ALARM_ID, alarmId);
            //bundle.putInt();
            fragment.setArguments(bundle);
            return fragment;
        }

        @Override
        public void onCreatePreferences(Bundle bundle, String s) {
            addPreferencesFromResource(R.xml.pref_alarm);

            Bundle args = getArguments();
            mAlarmId = UUID.fromString(args.getString(ARGS_ALARM_ID));
            mAlarm = AlarmList.get(getContext()).getAlarm(mAlarmId);

            mTimePreference = (TimePreference) findPreference("KEY_ALARM_TIME");
            mTimePreference.setTime(mAlarm.getTimeHour(), mAlarm.getTimeMinute());

            mRepeatingDaysPreference = (RepeatingDaysPreference) findPreference("KEY_ALARM_REPEATING_DAYS");
            CharSequence[] menuItems = mRepeatingDaysPreference.getEntryValues();
            Set<String> values = new HashSet<>();
            for (int i = 0; i < 7; ++i) {
                if (mAlarm.getRepeatingDay(i)) {
                    values.add(menuItems[i].toString());
                }
            }
            mRepeatingDaysPreference.setValues(values);

            mNamePreference = (NamePreference) findPreference("KEY_ALARM_NAME");
            mNamePreference.setText(mAlarm.getTitle());

            mGamesPreference = (GamesPreference) findPreference("KEY_ALARM_GAME");

            mRingtonePreference = (RingtonePreference) findPreference("KEY_ALARM_RINGTONE");

            mButtonsPreference = (ButtonsPreference) findPreference("KEY_ALARM_BUTTONS");
            mButtonsPreference.setLeftButtonText(getResources().getString(R.string.pref_button_cancel));
            mButtonsPreference.setRightButtonText(getResources().getString(R.string.pref_button_save));

            mButtonsPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    handleSettingsExit((boolean) o);
                    return true;
                }
            });

            setDefaultSummaryValues();
        }

        @Override
        public void onDisplayPreferenceDialog(Preference preference) {
            if (preference instanceof RepeatingDaysPreference) {
                DialogFragment dialogFragment = MultiSelectListPreferenceDialogFragmentCompat.newInstance(preference);
                dialogFragment.setTargetFragment(this, 0);
                dialogFragment.show(getFragmentManager(), "android.support.v7.preference.PreferenceFragment.DIALOG");
            } else if (preference instanceof TimePreference) {
                DialogFragment dialogFragment = TimePreferenceDialogFragmentCompat.newInstance(preference);
                dialogFragment.setTargetFragment(this, 0);
                dialogFragment.show(getFragmentManager(), "android.support.v7.preference.PreferenceFragment.DIALOG");
            } else if (preference instanceof NamePreference) {
                DialogFragment dialogFragment = NamePreferenceDialogFragmentCompat.newInstance(preference);
                dialogFragment.setTargetFragment(this, 0);
                dialogFragment.show(getFragmentManager(), "android.support.v7.preference.PreferenceFragment.DIALOG");
            } else if (preference instanceof GamesPreference) {
                DialogFragment dialogFragment = MultiSelectListPreferenceDialogFragmentCompat.newInstance(preference);
                dialogFragment.setTargetFragment(this, 0);
                dialogFragment.show(getFragmentManager(), "android.support.v7.preference.PreferenceFragment.DIALOG");
            } else super.onDisplayPreferenceDialog(preference);
        }



        private void setDefaultSummaryValues() {
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
                dirty = true;
            }

            if (mRingtonePreference.isDirty()) {
                dirty = true;
            }

            if (persistSettings && dirty) {
                AlarmManagerHelper.cancelAlarms(getContext());
                AlarmList.get(getActivity()).updateAlarm(mAlarm);
                AlarmManagerHelper.setAlarms(getContext());
            }

            getActivity().finish();
        }
    }
}
