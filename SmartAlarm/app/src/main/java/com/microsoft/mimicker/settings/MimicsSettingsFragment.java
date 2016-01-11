package com.microsoft.mimicker.settings;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.microsoft.mimicker.R;
import com.microsoft.mimicker.appcore.DividerItemDecoration;
import com.microsoft.mimicker.utilities.Logger;


public class MimicsSettingsFragment extends PreferenceFragmentCompat {
    public static final String MIMICS_SETTINGS_FRAGMENT_TAG = "mimics_settings_fragment";
    private static final String ARGS_ALARM_ID = "alarm_id";
    private static final String ARGS_ENABLED_MIMICS = "enabled_mimics";
    MimicsSettingsListener mCallback;
    private String mAlarmId;

    public static MimicsSettingsFragment newInstance(String alarmId, String[] enabledMimics) {
        MimicsSettingsFragment fragment = new MimicsSettingsFragment();
        Bundle bundle = new Bundle(1);
        bundle.putString(ARGS_ALARM_ID, alarmId);
        bundle.putStringArray(ARGS_ENABLED_MIMICS, enabledMimics);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallback = (MimicsSettingsListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
        Logger.flush();
    }

    @Override
    public void onCreatePreferences(Bundle bundle, final String s) {
        addPreferencesFromResource(R.xml.pref_mimics);

        Bundle args = getArguments();
        mAlarmId = args.getString(ARGS_ALARM_ID);
        String[] enabledMimics = args.getStringArray(ARGS_ENABLED_MIMICS);
        for (String mimic : enabledMimics) {

        }

    }

    @Override
    public RecyclerView onCreateRecyclerView(LayoutInflater inflater, ViewGroup parent,
                                             Bundle savedInstanceState) {
        LinearLayout rootLayout = (LinearLayout) parent.getParent();
        AppBarLayout appBarLayout =
                (AppBarLayout) LayoutInflater.from(getContext()).inflate(R.layout.settings_toolbar,
                        rootLayout,
                        false);
        rootLayout.addView(appBarLayout, 0); // insert at top
        Toolbar bar = (Toolbar) appBarLayout.findViewById(R.id.settings_toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(bar);
        bar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBack();
            }
        });
        bar.setTitle(R.string.pref_title_mimics);

        RecyclerView recyclerView = super.onCreateRecyclerView(inflater, parent, savedInstanceState);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(),
                DividerItemDecoration.VERTICAL_LIST));
        return recyclerView;
    }

    public void onBack() {
        mCallback.onMimicsSettingsDismiss(mAlarmId, getEnabledMimics());
    }

    private String[] getEnabledMimics() {
        return null;
    }

    public interface MimicsSettingsListener {
        void onMimicsSettingsDismiss(String alarmId, String[] enabledMimics);
    }
}

