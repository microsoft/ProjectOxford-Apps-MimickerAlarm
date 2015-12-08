package com.microsoft.smartalarm;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceManager;
import android.view.MenuItem;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.FeedbackManager;
import net.hockeyapp.android.UpdateManager;
import net.hockeyapp.android.objects.FeedbackUserDataElement;

public class AlarmListActivity extends SingleFragmentActivity
        implements AlarmListFragment.Callbacks {

    protected Fragment createFragment() {
        return new AlarmListFragment();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_masterdetail;
    }

    @Override
    public void onAlarmSelected(Alarm alarm, boolean newAlarm) {
        Intent intent = AlarmSettingsActivity.newIntent(this, alarm.getId(), newAlarm);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final String hockeyappToken = Util.getToken(this, "hockeyapp");
        UpdateManager.register(this, hockeyappToken);
        PreferenceManager.setDefaultValues(this, R.xml.pref_global, false);
        Logger.init(this);
        setTitle(R.string.alarm_list_title);
    }

    @Override
    protected void onResume() {
        super.onResume();
        final String hockeyappToken = Util.getToken(this, "hockeyapp");
        CrashManager.register(this, hockeyappToken);
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
        Logger.flush();
    }

    public void showFeedback(MenuItem item){
        final String hockeyappToken = Util.getToken(this, "hockeyapp");
        FeedbackManager.register(this, hockeyappToken, null);
        FeedbackManager.setRequireUserEmail(FeedbackUserDataElement.OPTIONAL);
        FeedbackManager.showFeedbackActivity(this);
    }
}
