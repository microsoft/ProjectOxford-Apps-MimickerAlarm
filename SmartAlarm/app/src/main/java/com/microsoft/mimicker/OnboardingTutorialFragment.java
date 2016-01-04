package com.microsoft.mimicker;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class OnboardingTutorialFragment extends Fragment {
    private static final int WELCOME_MSG_DURATION = 1500;
    private static final int WELCOME_MSG_CROSSFADE_DURATION = 1000;
    OnOnboardingTutorialListener mCallback;
    private Boolean mStarted = false;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallback = (OnOnboardingTutorialListener) context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_onboarding, container, false);

        OnboardingPagerAdapter onboardingPagerAdapter = new OnboardingPagerAdapter(getChildFragmentManager());
        ViewPager viewPager = (ViewPager) rootView.findViewById(R.id.onboarding_pager);
        viewPager.setAdapter(onboardingPagerAdapter);
        BubblePagerIndicator indicator = (BubblePagerIndicator) rootView.findViewById(R.id.onboarding_indicator);
        indicator.setTotalPositions(onboardingPagerAdapter.getCount());
        viewPager.addOnPageChangeListener(indicator);
        Button skip = (Button) rootView.findViewById(R.id.skip_button);
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                skipToToS();
            }
        });
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        final View welcomePage = getView().findViewById(R.id.onboarding_welcome);
        View tutorialContainer = getView().findViewById(R.id.onboarding_tutorial);
        if (!mStarted) {
            mStarted = true;
            tutorialContainer.setAlpha(0f);
            tutorialContainer.setVisibility(View.VISIBLE);
            tutorialContainer.animate()
                    .alpha(1f)
                    .setDuration(WELCOME_MSG_CROSSFADE_DURATION)
                    .setStartDelay(WELCOME_MSG_DURATION)
                    .setListener(null);
            welcomePage.animate()
                    .alpha(0)
                    .setDuration(WELCOME_MSG_CROSSFADE_DURATION)
                    .setStartDelay(WELCOME_MSG_DURATION)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            welcomePage.setVisibility(View.GONE);
                        }
                    });
        }
        else{
            tutorialContainer.setAlpha(1f);
        }
    }

    public void skipToToS () {
        mCallback.onSkip();
    }

    public interface OnOnboardingTutorialListener {
        void onSkip();
    }

    private static class OnboardingPagerAdapter extends FragmentStatePagerAdapter {
        public OnboardingPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = new OnboardingPageFragment();
            Bundle args = new Bundle();
            args.putInt(OnboardingPageFragment.POSITION, position);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return 4;
        }
    }

    public static class OnboardingPageFragment extends Fragment {
        public static final String POSITION = "position";

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_onboarding_page, container, false);
            Bundle args = getArguments();
            Integer imageResId = 0;
            Integer titleResId = 0;
            Integer textResId = 0;

            switch (args.getInt(POSITION)){
                case 0:
                    imageResId = R.drawable.onboarding_tutorial_1;
                    titleResId = R.string.onboarding_tutorial_title_1;
                    textResId = R.string.onboarding_tutorial_text_1;
                    break;
                case 1:
                    imageResId = R.drawable.onboarding_tutorial_2;
                    titleResId = R.string.onboarding_tutorial_title_2;
                    textResId = R.string.onboarding_tutorial_text_2;
                    break;
                case 2:
                    imageResId = R.drawable.onboarding_tutorial_game_animate1;
                    titleResId = R.string.onboarding_tutorial_title_3;
                    textResId = R.string.onboarding_tutorial_text_3;
                    break;
                case 3:
                    imageResId = R.drawable.onboarding_tutorial_4;
                    titleResId = R.string.onboarding_tutorial_title_4;
                    textResId = R.string.onboarding_tutorial_text_4;
                    break;
            }

            ((ImageView) rootView.findViewById(R.id.onboarding_image)).setImageResource(imageResId);
            ((TextView) rootView.findViewById(android.R.id.text1)).setText(titleResId);
            ((TextView) rootView.findViewById(android.R.id.text2)).setText(textResId);
            if (args.getInt(POSITION) == 3){
                View nextButton = rootView.findViewById(android.R.id.button1);
                nextButton.setVisibility(View.VISIBLE);
                nextButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        OnboardingTutorialFragment parent = (OnboardingTutorialFragment) getParentFragment();
                        parent.skipToToS();
                    }
                });
            }
            return rootView;
        }

    }
}

