package com.microsoft.mimicker.onboarding;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.microsoft.mimicker.R;
import com.microsoft.mimicker.appcore.AlarmMainActivity;
import com.microsoft.mimicker.utilities.Loggable;
import com.microsoft.mimicker.utilities.Logger;
import com.microsoft.mimicker.utilities.GeneralUtilities;

public class OnboardingToSFragment extends Fragment {
    public static final String TOS_FRAGMENT_TAG = "tos_fragment";
    OnOnboardingToSListener mCallback;
    View mRootToSView;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallback = (OnOnboardingToSListener) context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Logger.init(getActivity());

        mRootToSView = inflater.inflate(R.layout.fragment_onboarding_tos, container, false);
        mRootToSView.findViewById(R.id.onboarding_tos_gotit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transitionToSPage(R.id.onboarding_tos_acceptance_before, R.id.onboarding_tos_acceptance);
            }
        });

        mRootToSView.findViewById(R.id.onboarding_tos_accept).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Loggable.UserAction userAction = new Loggable.UserAction(Loggable.Key.ACTION_ONBOARDING_TOS_ACCEPT);
                Logger.track(userAction);
                String packageName = getActivity().getApplication().getPackageName();
                SharedPreferences preferences = getActivity().getSharedPreferences(packageName, Context.MODE_PRIVATE);
                preferences.edit().putBoolean(AlarmMainActivity.SHOULD_TOS, false).apply();
                mCallback.onAccept();
            }
        });

        mRootToSView.findViewById(R.id.onboarding_tos_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Loggable.UserAction userAction = new Loggable.UserAction(Loggable.Key.ACTION_ONBOARDING_TOS_DECLINE);
                Logger.track(userAction);
                transitionToSPage(R.id.onboarding_tos_acceptance, R.id.onboarding_tos_acceptance_reminder);
            }
        });

        mRootToSView.findViewById(R.id.onboarding_tos_gonow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transitionToSPage(R.id.onboarding_tos_acceptance_reminder, R.id.onboarding_tos_acceptance);
            }
        });

        GeneralUtilities.enableLinks((TextView) mRootToSView.findViewById(R.id.onboarding_tos_text));
        return mRootToSView;
    }

    private void transitionToSPage(int resourceIdFromPage, int resourceIdToPage) {
        mRootToSView.findViewById(resourceIdFromPage).setVisibility(View.GONE);
        mRootToSView.findViewById(resourceIdToPage).setVisibility(View.VISIBLE);
    }

    public interface OnOnboardingToSListener {
        void onAccept();
    }
}

