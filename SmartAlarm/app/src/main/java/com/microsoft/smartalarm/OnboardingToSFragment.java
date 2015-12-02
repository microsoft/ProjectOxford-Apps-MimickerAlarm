package com.microsoft.smartalarm;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class OnboardingToSFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_onboarding_tos, container, false);
        rootView.findViewById(R.id.onboarding_tos_gotit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rootView.findViewById(R.id.onboarding_tos_1).setVisibility(View.GONE);
                rootView.findViewById(R.id.onboarding_tos_2).setVisibility(View.VISIBLE);
                getActivity().findViewById(R.id.onboarding_container).setBackgroundColor(getResources().getColor(R.color.white));
            }
        });
        rootView.findViewById(R.id.onboarding_tos_accept).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String packageName = getActivity().getApplication().getPackageName();
                SharedPreferences preferences = getActivity().getSharedPreferences(packageName, Context.MODE_PRIVATE);
                preferences.edit().putBoolean(OnboardingActivity.SHOULD_TOS, false).apply();
                Intent startMainActivity = new Intent(getActivity(), AlarmListActivity.class);
                startActivity(startMainActivity);
            }
        });
        rootView.findViewById(R.id.onboarding_tos_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rootView.findViewById(R.id.onboarding_tos_2).setVisibility(View.GONE);
                rootView.findViewById(R.id.onboarding_tos_3).setVisibility(View.VISIBLE);
                getActivity().findViewById(R.id.onboarding_container).setBackgroundColor(getResources().getColor(R.color.green1));
            }
        });
        rootView.findViewById(R.id.onboarding_tos_gonow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rootView.findViewById(R.id.onboarding_tos_3).setVisibility(View.GONE);
                rootView.findViewById(R.id.onboarding_tos_2).setVisibility(View.VISIBLE);
                getActivity().findViewById(R.id.onboarding_container).setBackgroundColor(getResources().getColor(R.color.white));
            }
        });
        return rootView;
    }
}

