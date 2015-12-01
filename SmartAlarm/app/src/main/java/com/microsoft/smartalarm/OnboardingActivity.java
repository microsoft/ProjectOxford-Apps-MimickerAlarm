package com.microsoft.smartalarm;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class OnboardingActivity extends FragmentActivity{
    private SharedPreferences mPreferences = null;
    private boolean mStarted = false;
    public final static String SHOULD_ONBOARD = "onboarding";
    public final static String SHOULD_TOS = "show-tos";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        Logger.init(this);

        String packageName = getApplicationContext().getPackageName();
        mPreferences = getSharedPreferences(packageName, MODE_PRIVATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPreferences.getBoolean(SHOULD_ONBOARD, true)) {
            setStatusBarColor();
            mPreferences.edit().putBoolean(SHOULD_ONBOARD, false).apply();
            if (!mStarted) {
                mStarted = true;
                Logger.trackUserAction(Logger.UserAction.FIRST_RUN, null, null);

                Fragment newFragment = new OnboardingTutorialFragment();
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.onboarding_container, newFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        }
        else if (mPreferences.getBoolean(SHOULD_TOS, true)) {
            setStatusBarColor();
            showToS(null);
        }
        else {
            Intent startMainActivity = new Intent(this, AlarmListActivity.class);
            startActivity(startMainActivity);
        }
    }

    public void showToS(View view) {
        Fragment newFragment = new OnboardingToSFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.onboarding_container, newFragment);
        if (view != null){
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }

    private void setStatusBarColor(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.green1));
        }
    }

}
