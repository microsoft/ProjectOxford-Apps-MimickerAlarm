package com.microsoft.mimicker.appcore;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.MenuItem;

import com.microsoft.mimicker.BuildConfig;
import com.microsoft.mimicker.R;
import com.microsoft.mimicker.model.Alarm;
import com.microsoft.mimicker.onboarding.OnboardingToSFragment;
import com.microsoft.mimicker.onboarding.OnboardingTutorialFragment;
import com.microsoft.mimicker.scheduling.AlarmNotificationManager;
import com.microsoft.mimicker.scheduling.AlarmScheduler;
import com.microsoft.mimicker.settings.AlarmSettingsFragment;
import com.microsoft.mimicker.settings.MimicsSettingsFragment;
import com.microsoft.mimicker.utilities.GeneralUtilities;
import com.microsoft.mimicker.utilities.Loggable;
import com.microsoft.mimicker.utilities.Logger;
import com.microsoft.mimicker.utilities.KeyUtil;

import net.hockeyapp.android.FeedbackManager;
import net.hockeyapp.android.UpdateManager;
import net.hockeyapp.android.objects.FeedbackUserDataElement;

import java.util.ArrayList;
import java.util.UUID;

public class AlarmMainActivity extends AppCompatActivity
        implements AlarmListFragment.AlarmListListener,
        OnboardingTutorialFragment.OnOnboardingTutorialListener,
        OnboardingToSFragment.OnOnboardingToSListener,
        AlarmSettingsFragment.AlarmSettingsListener,
        MimicsSettingsFragment.MimicsSettingsListener {

    public final static String SHOULD_ONBOARD = "onboarding";
    public final static String SHOULD_TOS = "show-tos";
    private SharedPreferences mPreferences = null;
    private AudioManager mAudioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);
        String packageName = getApplicationContext().getPackageName();
        mPreferences = getSharedPreferences(packageName, MODE_PRIVATE);
        PreferenceManager.setDefaultValues(this, R.xml.pref_global, false);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        AlarmNotificationManager.get(this).handleNextAlarmNotificationStatus();

        UUID alarmId = (UUID) getIntent().getSerializableExtra(AlarmScheduler.ALARM_ID);
        if (alarmId != null) {
            showAlarmSettingsFragment(alarmId.toString());
        }

        Logger.init(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        UUID alarmId = (UUID) intent.getSerializableExtra(AlarmScheduler.ALARM_ID);
        if (alarmId != null) {
            showAlarmSettingsFragment(alarmId.toString());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        final String hockeyappToken = KeyUtil.getToken(this, "hockeyapp");
        if (!BuildConfig.DEBUG)
            UpdateManager.register(this, hockeyappToken);
        GeneralUtilities.registerCrashReport(this);

        if (mPreferences.getBoolean(SHOULD_ONBOARD, true)) {
            if (!hasOnboardingStarted()) {
                Loggable.UserAction userAction = new Loggable.UserAction(Loggable.Key.ACTION_ONBOARDING);
                Logger.track(userAction);

                showTutorial(null);
            }
        }
        else if (mPreferences.getBoolean(SHOULD_TOS, true)) {
            showToS();
        } else if (!areEditingSettings()) {
            GeneralUtilities.showFragment(getSupportFragmentManager(),
                    new AlarmListFragment(),
                    AlarmListFragment.ALARM_LIST_FRAGMENT_TAG);
            setTitle(R.string.alarm_list_title);
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
        final String hockeyappToken = KeyUtil.getToken(this, "hockeyapp");
        FeedbackManager.register(this, hockeyappToken, null);
        FeedbackManager.setRequireUserEmail(FeedbackUserDataElement.OPTIONAL);
        FeedbackManager.showFeedbackActivity(this);
    }

    public void showTutorial(MenuItem item){
        if (item != null) {
            GeneralUtilities.showFragmentFromLeft(getSupportFragmentManager(),
                    new OnboardingTutorialFragment(),
                    OnboardingTutorialFragment.ONBOARDING_FRAGMENT_TAG);
        } else {
            GeneralUtilities.showFragment(getSupportFragmentManager(),
                    new OnboardingTutorialFragment(),
                    OnboardingTutorialFragment.ONBOARDING_FRAGMENT_TAG);
        }
    }

    @Override
    public void onSkip() {
        if (mPreferences.getBoolean(SHOULD_TOS, true)) {
            Loggable.UserAction userAction = new Loggable.UserAction(Loggable.Key.ACTION_ONBOARDING_SKIP);
            Logger.track(userAction);
            showToS();
        }
        else {
            GeneralUtilities.showFragmentFromRight(getSupportFragmentManager(),
                    new AlarmListFragment(),
                    AlarmListFragment.ALARM_LIST_FRAGMENT_TAG);
            setTitle(R.string.alarm_list_title);
        }
    }

    @Override
    public void onAccept() {
        GeneralUtilities.showFragmentFromRight(getSupportFragmentManager(),
                new AlarmListFragment(),
                AlarmListFragment.ALARM_LIST_FRAGMENT_TAG);
        setTitle(R.string.alarm_list_title);
    }

    @Override
    public void onBackPressed() {
        if (areEditingSettings()) {
            if (areEditingMimicSettings()) {
                ((MimicsSettingsFragment) getSupportFragmentManager()
                        .findFragmentByTag(MimicsSettingsFragment.MIMICS_SETTINGS_FRAGMENT_TAG))
                        .onBack();
            } else {
                ((AlarmSettingsFragment) getSupportFragmentManager()
                        .findFragmentByTag(AlarmSettingsFragment.SETTINGS_FRAGMENT_TAG))
                        .onCancel();
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            mAudioManager.adjustStreamVolume(AudioManager.STREAM_ALARM,
                    AudioManager.ADJUST_LOWER,
                    AudioManager.FLAG_SHOW_UI | AudioManager.FLAG_PLAY_SOUND);
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            mAudioManager.adjustStreamVolume(AudioManager.STREAM_ALARM,
                    AudioManager.ADJUST_RAISE,
                    AudioManager.FLAG_SHOW_UI | AudioManager.FLAG_PLAY_SOUND);
        } else {
            return super.onKeyDown(keyCode, event);
        }
        return true;
    }

    public void showToS() {
        mPreferences.edit().putBoolean(SHOULD_ONBOARD, false).apply();
        GeneralUtilities.showFragment(getSupportFragmentManager(),
                new OnboardingToSFragment(),
                OnboardingToSFragment.TOS_FRAGMENT_TAG);
    }

    private boolean areEditingSettings() {
        return (getSupportFragmentManager()
                .findFragmentByTag(AlarmSettingsFragment.SETTINGS_FRAGMENT_TAG) != null) ||
                (getSupportFragmentManager()
                        .findFragmentByTag(MimicsSettingsFragment.MIMICS_SETTINGS_FRAGMENT_TAG) != null);
    }

    private boolean areEditingMimicSettings() {
        return (getSupportFragmentManager()
                .findFragmentByTag(MimicsSettingsFragment.MIMICS_SETTINGS_FRAGMENT_TAG) != null);
    }

    private boolean hasOnboardingStarted() {
        return (getSupportFragmentManager()
                .findFragmentByTag(OnboardingTutorialFragment.ONBOARDING_FRAGMENT_TAG) != null);
    }

    @Override
    public void onSettingsSaveOrIgnoreChanges() {
        GeneralUtilities.showFragmentFromLeft(getSupportFragmentManager(),
                new AlarmListFragment(),
                AlarmListFragment.ALARM_LIST_FRAGMENT_TAG);
        setTitle(R.string.alarm_list_title);
        onAlarmChanged();
    }

    @Override
    public void onSettingsDeleteOrNewCancel() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.anim.fade_in, R.anim.slide_down);
        transaction.replace(R.id.fragment_container, new AlarmListFragment());
        transaction.commit();
        setTitle(R.string.alarm_list_title);
        onAlarmChanged();
    }

    @Override
    public void onMimicsSettingsDismiss(String alarmId, ArrayList<String> enabledMimics) {
        GeneralUtilities.showFragmentFromLeft(getSupportFragmentManager(),
                AlarmSettingsFragment.newInstance(alarmId, enabledMimics),
                AlarmSettingsFragment.SETTINGS_FRAGMENT_TAG);
    }

    @Override
    public void onShowMimicsSettings(String alarmId, ArrayList<String> enabledMimics) {
        GeneralUtilities.showFragmentFromRight(getSupportFragmentManager(),
                MimicsSettingsFragment.newInstance(alarmId, enabledMimics),
                MimicsSettingsFragment.MIMICS_SETTINGS_FRAGMENT_TAG);
    }

    @Override
    public void onAlarmSelected(Alarm alarm) {
        showAlarmSettingsFragment(alarm.getId().toString());
    }

    @Override
    public void onAlarmChanged() {
        AlarmNotificationManager.get(this).handleNextAlarmNotificationStatus();
    }

    private void showAlarmSettingsFragment(String alarmId) {
        GeneralUtilities.showFragmentFromRight(getSupportFragmentManager(),
                AlarmSettingsFragment.newInstance(alarmId),
                AlarmSettingsFragment.SETTINGS_FRAGMENT_TAG);
    }
}
