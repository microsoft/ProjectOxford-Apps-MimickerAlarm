package com.microsoft.smartalarm;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.view.MenuItem;

import net.hockeyapp.android.FeedbackManager;
import net.hockeyapp.android.UpdateManager;
import net.hockeyapp.android.objects.FeedbackUserDataElement;

public class AlarmMainActivity extends AppCompatActivity
        implements AlarmListFragment.AlarmListListener,
        OnboardingTutorialFragment.OnOnboardingTutorialListener,
        OnboardingToSFragment.OnOnboardingToSListener,
        AlarmSettingsFragment.AlarmSettingsListener {

    private boolean mEditingAlarm = false;
    private boolean mOboardingStarted = false;
    public final static String SHOULD_ONBOARD = "onboarding";
    public final static String SHOULD_TOS = "show-tos";
    private SharedPreferences mPreferences = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);
        String packageName = getApplicationContext().getPackageName();
        mPreferences = getSharedPreferences(packageName, MODE_PRIVATE);
        PreferenceManager.setDefaultValues(this, R.xml.pref_global, false);
        Logger.init(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        final String hockeyappToken = Util.getToken(this, "hockeyapp");
        if (!BuildConfig.DEBUG)
            UpdateManager.register(this, hockeyappToken);
        Util.registerCrashReport(this);

        if (mPreferences.getBoolean(SHOULD_ONBOARD, true)) {
            if (!mOboardingStarted) {
                mOboardingStarted = true;

                Loggable.UserAction userAction = new Loggable.UserAction(Loggable.Key.ACTION_ONBOARDING);
                Logger.track(userAction);

                showTutorial(null);
            }
        }
        else if (mPreferences.getBoolean(SHOULD_TOS, true)) {
            showToS();
        }
        else if (!mEditingAlarm) {
            showAlarmList(false);
        }
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

    public void showTutorial(MenuItem item){
        showFragment(new OnboardingTutorialFragment());
    }

    @Override
    public void onSkip() {
        if (mPreferences.getBoolean(SHOULD_TOS, true)) {
            Loggable.UserAction userAction = new Loggable.UserAction(Loggable.Key.ACTION_ONBOARDING_SKIP);
            Logger.track(userAction);
            showToS();
        }
        else {
            showAlarmList(false);
        }
    }

    @Override
    public void onAccept() {
        showAlarmList(false);
    }

    @Override
    public void onBackPressed() {
        if (mEditingAlarm) {
            ((AlarmSettingsFragment)getSupportFragmentManager()
                    .findFragmentByTag(AlarmSettingsFragment.SETTINGS_FRAGMENT_TAG))
                    .onCancel();
        } else {
            super.onBackPressed();
        }
    }

    public void showToS() {
        mPreferences.edit().putBoolean(SHOULD_ONBOARD, false).apply();
        showFragment(new OnboardingToSFragment());
    }

    public void showAlarmList(boolean animateEntrance) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (animateEntrance) {
            transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
        }
        transaction.replace(R.id.fragment_container, new AlarmListFragment());
        transaction.commit();
        setTitle(R.string.alarm_list_title);
    }

    @Override
    public void onSettingsSaveOrIgnoreChanges() {
        showAlarmList(true);
        mEditingAlarm = false;
    }

    @Override
    public void onSettingsDeleteOrNewCancel() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        transaction.replace(R.id.fragment_container, new AlarmListFragment());
        transaction.commit();
        setTitle(R.string.alarm_list_title);
    }

    @Override
    public void onAlarmSelected(Alarm alarm) {
        showAlarmSettingsFragment(alarm.getId().toString());
        mEditingAlarm = true;
    }

    private void showFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    private void showAlarmSettingsFragment(String alarmId) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
        transaction.replace(R.id.fragment_container,
                            AlarmSettingsFragment.newInstance(alarmId),
                            AlarmSettingsFragment.SETTINGS_FRAGMENT_TAG);
        transaction.commit();
    }
}
