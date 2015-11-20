package com.microsoft.smartalarm;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.PreferenceFragmentCompat;

public class AlarmGlobalSettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, new AlarmGlobalSettingsFragment())
                .commit();
    }

    public static class AlarmGlobalSettingsFragment extends PreferenceFragmentCompat
            implements SharedPreferences.OnSharedPreferenceChangeListener {

        private ListPreference mSnoozeDuration;
        private ListPreference mRingDuration;

        @Override
        public void onCreatePreferences(Bundle bundle, String s) {
            addPreferencesFromResource(R.xml.pref_global);
            mSnoozeDuration = (ListPreference)findPreference("KEY_SNOOZE_DURATION");
            mRingDuration = (ListPreference)findPreference("KEY_RING_DURATION");
            setDefaultSummaryValues();
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals("KEY_SNOOZE_DURATION")) {
                mSnoozeDuration.setSummary(mSnoozeDuration.getEntry());
            } else if (key.equals("KEY_RING_DURATION")) {
                mRingDuration.setSummary(mRingDuration.getEntry());
            }
        }

        private void setDefaultSummaryValues() {
            mSnoozeDuration.setSummary(mSnoozeDuration.getEntry());
            mRingDuration.setSummary(mRingDuration.getEntry());
        }
    }
}
