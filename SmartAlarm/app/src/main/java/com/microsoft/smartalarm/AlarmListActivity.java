package com.microsoft.smartalarm;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.Fragment;
import android.view.MenuItem;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.FeedbackManager;
import net.hockeyapp.android.UpdateManager;
import net.hockeyapp.android.objects.FeedbackUserDataElement;

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

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        final String hockeyAppId = getResources().getString(R.string.hockeyapp_id);
        UpdateManager.register(this, hockeyAppId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        final String hockeyAppId = getResources().getString(R.string.hockeyapp_id);
        CrashManager.register(this, hockeyAppId);
    }

    @Override
    protected void onPause() {
        super.onPause();
        UpdateManager.unregister();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FeedbackManager.unregister();
    }

    public void showFeedback(MenuItem item){
        final String hockeyAppId = getResources().getString(R.string.hockeyapp_id);
        FeedbackManager.register(this, hockeyAppId, null);
        FeedbackManager.setRequireUserEmail(FeedbackUserDataElement.OPTIONAL);
        FeedbackManager.showFeedbackActivity(this);
    }
}
