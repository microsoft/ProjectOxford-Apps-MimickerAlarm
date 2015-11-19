package com.microsoft.smartalarm;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceManager;

public class AlarmListActivity extends SingleFragmentActivity
        implements AlarmListFragment.Callbacks, AlarmFragment.Callbacks {

    protected Fragment createFragment() {
        return new AlarmListFragment();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_masterdetail;
    }

    @Override
    public void onAlarmSelected(Alarm alarm) {

        Intent intent = AlarmPagerActivity.newIntent(this, alarm.getId());
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, R.xml.pref_global, false);
    }

    @Override
    public void onAlarmUpdated(Alarm alarm) {
        AlarmListFragment listFragment = (AlarmListFragment)
                getSupportFragmentManager()
                        .findFragmentById(R.id.fragment_container);
        listFragment.updateUI();
    }
}
