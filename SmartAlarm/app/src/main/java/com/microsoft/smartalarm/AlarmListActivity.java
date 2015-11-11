package com.microsoft.smartalarm;

import android.content.Intent;
import android.support.v4.app.Fragment;

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
    public void onAlarmUpdated(Alarm alarm) {
        AlarmListFragment listFragment = (AlarmListFragment)
                getSupportFragmentManager()
                        .findFragmentById(R.id.fragment_container);
        listFragment.updateUI();
    }
}
