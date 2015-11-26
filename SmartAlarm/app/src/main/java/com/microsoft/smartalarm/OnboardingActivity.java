package com.microsoft.smartalarm;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class OnboardingActivity extends FragmentActivity implements ViewPager.OnPageChangeListener {

    private SharedPreferences mPreferences = null;
    private OnboardingPagerAdapter mOnboardingPagerAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);
        Logger.init(this);

        String packageName = getApplicationContext().getPackageName();
        mPreferences = getSharedPreferences(packageName, MODE_PRIVATE);
        mOnboardingPagerAdapter = new OnboardingPagerAdapter(getSupportFragmentManager());
        ViewPager viewPager = (ViewPager) findViewById(R.id.onboarding_pager);
        viewPager.setAdapter(mOnboardingPagerAdapter);
        viewPager.addOnPageChangeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mPreferences.getBoolean("firstrun", true) || true) {
            mPreferences.edit().putBoolean("firstrun", false).apply();
            Logger.trackUserAction(Logger.UserAction.FIRST_RUN, null, null);
            crossfade();
        }
        else {
            Intent startMainActivity = new Intent(this, AlarmListActivity.class);
            startActivity(startMainActivity);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        Log.d("OnboardingActivitiy", "Position : " + String.valueOf(position));
        Log.d("OnboardingActivitiy", "Position Offset : " + String.valueOf(positionOffset));
    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {
        Log.d("OnboardingActivitiy", "state : " + String.valueOf(state));
    }

    private void crossfade(){
        final TextView firstText = (TextView)findViewById(R.id.first_text);
        ViewPager viewPager = (ViewPager) findViewById(R.id.onboarding_pager);
        viewPager.setAlpha(0f);
        viewPager.setVisibility(View.VISIBLE);

        viewPager.animate().alpha(1f).setDuration(1000).setStartDelay(1000).setListener(null);
        firstText.animate().alpha(0).setDuration(1000).setStartDelay(1000).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                firstText.setVisibility(View.GONE);
            }
        });
    }

    private static class OnboardingPagerAdapter extends FragmentStatePagerAdapter{
        public OnboardingPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = new OnboardingPageFragment();
            Bundle args = new Bundle();
            args.putInt(OnboardingPageFragment.ARG_OBJECT, position + 1);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return 5;
        }
    }

    public static class OnboardingPageFragment extends Fragment {

        public static final String ARG_OBJECT = "object";

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_onboarding, container, false);
            Bundle args = getArguments();
            ((TextView) rootView.findViewById(android.R.id.text1)).setText(
                    Integer.toString(args.getInt(ARG_OBJECT)));
            return rootView;
        }
    }
}
