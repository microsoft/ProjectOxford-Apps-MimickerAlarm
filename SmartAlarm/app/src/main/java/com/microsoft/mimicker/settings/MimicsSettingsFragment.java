package com.microsoft.mimicker.settings;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.microsoft.mimicker.R;
import com.microsoft.mimicker.appcore.DividerItemDecoration;
import com.microsoft.mimicker.utilities.GeneralUtilities;
import com.microsoft.mimicker.utilities.Logger;
import com.microsoft.mimicker.utilities.SettingsUtilities;

import java.util.ArrayList;

/**
 * This is a special PreferenceFragment class that lists the different Mimic settings for an
 * alarm.  The list of Mimics is populated from pref_mimics.xml/
 */
public class MimicsSettingsFragment extends PreferenceFragmentCompat {
    public static final String MIMICS_SETTINGS_FRAGMENT_TAG = "mimics_settings_fragment";
    private static final String ARGS_ENABLED_MIMICS = "enabled_mimics";
    MimicsSettingsListener mCallback;

    public static MimicsSettingsFragment newInstance(ArrayList<String> enabledMimics) {
        MimicsSettingsFragment fragment = new MimicsSettingsFragment();
        Bundle bundle = new Bundle(1);
        bundle.putStringArrayList(ARGS_ENABLED_MIMICS, enabledMimics);
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
    public void onPause() {
        super.onPause();
        // We need to pass the enabled Mimics to the Alarm Settings if we are dismissed
        // using the back button with Alarm Settings already on the backstack
        if (launchedFromAlarmSettings()) {
            onBack();
        }
    }

    @Override
    public void onCreatePreferences(Bundle bundle, final String s) {
        addPreferencesFromResource(R.xml.pref_mimics);
        setDefaultEnabledState();

        Bundle args = getArguments();
        ArrayList<String> enabledMimics = args.getStringArrayList(ARGS_ENABLED_MIMICS);
        for (String mimicId : enabledMimics) {
            ((CheckBoxPreference)findPreference(mimicId)).setChecked(true);
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
                // If Alarm settings is already in the backstack just pop, otherwise callback
                if (launchedFromAlarmSettings()) {
                    getFragmentManager().popBackStack();
                } else {
                    onBack();
                }

            }
        });
        bar.setTitle(R.string.pref_title_mimics);

        RecyclerView recyclerView = super.onCreateRecyclerView(inflater, parent, savedInstanceState);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(),
                DividerItemDecoration.VERTICAL_LIST));
        return recyclerView;
    }

    public void onBack() {
        mCallback.onMimicsSettingsDismiss(getEnabledMimics());
    }

    private void setDefaultEnabledState() {
        PreferenceScreen preferenceScreen = getPreferenceManager().getPreferenceScreen();
        for (int i = 0; i < preferenceScreen.getPreferenceCount(); i++) {
            ((CheckBoxPreference)preferenceScreen.getPreference(i)).setChecked(false);
        }

        if (!GeneralUtilities.deviceHasFrontFacingCamera()) {
            Preference preference = findPreference(getString(R.string.pref_mimic_express_yourself_id));
            preference.setEnabled(false);
            preference.setSummary(R.string.pref_mimic_not_supported);
        }
        if (!GeneralUtilities.deviceHasRearFacingCamera()) {
            Preference preference = findPreference(getString(R.string.pref_mimic_color_capture_id));
            preference.setEnabled(false);
            preference.setSummary(R.string.pref_mimic_not_supported);
        }
    }

    private ArrayList<String> getEnabledMimics() {
        ArrayList<String> enabledMimics = new ArrayList<>();
        PreferenceScreen preferenceScreen = getPreferenceManager().getPreferenceScreen();
        for (int i = 0; i < preferenceScreen.getPreferenceCount(); i++) {
            CheckBoxPreference preference = (CheckBoxPreference)preferenceScreen.getPreference(i);
            if (preference.isChecked()) {
                enabledMimics.add(preference.getKey());
            }
        }
        return enabledMimics;
    }

    private boolean launchedFromAlarmSettings() {
        return (SettingsUtilities.getAlarmSettingsFragment(getFragmentManager()) != null);
    }

    public interface MimicsSettingsListener {
        void onMimicsSettingsDismiss(ArrayList<String> enabledMimics);
    }
}

