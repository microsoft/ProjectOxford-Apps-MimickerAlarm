package com.microsoft.mimicker.globalsettings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.microsoft.mimicker.R;

import java.util.List;

public class AlarmGlobalSettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, new AlarmGlobalSettingsFragment())
                .commit();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) ||
            (keyCode == KeyEvent.KEYCODE_VOLUME_UP)) {
            ((AlarmGlobalSettingsFragment)fragments.get(0)).onKeyDown(keyCode);
        } else {
            return super.onKeyDown(keyCode, event);
        }
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    public static class AlarmGlobalSettingsFragment extends PreferenceFragmentCompat
            implements SharedPreferences.OnSharedPreferenceChangeListener {

        private ListPreference mSnoozeDuration;
        private ListPreference mRingDuration;
        private VolumeSliderPreference mAlarmVolume;

        @Override
        public void onCreatePreferences(Bundle bundle, String s) {
            addPreferencesFromResource(R.xml.pref_global);
            mSnoozeDuration = (ListPreference)findPreference("KEY_SNOOZE_DURATION");
            mRingDuration = (ListPreference)findPreference("KEY_RING_DURATION");
            mAlarmVolume = (VolumeSliderPreference)findPreference("KEY_RING_VOLUME");
            setDefaultSummaryValues();
        }

        @Override
        public RecyclerView onCreateRecyclerView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
            LinearLayout rootLayout = (LinearLayout) parent.getParent();
            AppBarLayout appBarLayout = (AppBarLayout) LayoutInflater.from(getContext()).inflate(R.layout.settings_toolbar, rootLayout, false);
            rootLayout.addView(appBarLayout, 0); // insert at top
            Toolbar bar = (Toolbar) appBarLayout.findViewById(R.id.settings_toolbar);
            ((AppCompatActivity) getActivity()).setSupportActionBar(bar);
            RecyclerView recyclerView =  super.onCreateRecyclerView(inflater, parent, savedInstanceState);
            return recyclerView;
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
                PreferenceManager.getDefaultSharedPreferences(getActivity())
                        .edit()
                        .putString("KEY_SNOOZE_DURATION_DISPLAY", mSnoozeDuration.getEntry().toString())
                        .apply();
            } else if (key.equals("KEY_RING_DURATION")) {
                mRingDuration.setSummary(mRingDuration.getEntry());
            }
        }

        public void onKeyDown(int keyCode) {
            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
                mAlarmVolume.decreaseVolume();
            } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                mAlarmVolume.increaseVolume();
            }
        }

        private void setDefaultSummaryValues() {
            mSnoozeDuration.setSummary(mSnoozeDuration.getEntry());
            mRingDuration.setSummary(mRingDuration.getEntry());
        }
    }
}
