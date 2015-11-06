package com.microsoft.smartalarm;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TimePicker;

import java.sql.Time;
import java.util.UUID;

public class AlarmFragment extends Fragment {
    private static final String ARG_ALARM_ID = "alarm_id";

    private TimePicker mTimePicker;
    private EditText mTitleField;
    private Alarm mAlarm;
    private Callbacks mCallbacks;

    public interface Callbacks {
        void onAlarmUpdated(Alarm alarm);
    }

    public static AlarmFragment newInstance(UUID alarmId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_ALARM_ID, alarmId);

        AlarmFragment fragment = new AlarmFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallbacks = (Callbacks)activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UUID alarmId = (UUID) getArguments().getSerializable(ARG_ALARM_ID);
        mAlarm = AlarmList.get(getActivity()).getAlarm(alarmId);
    }

    @Override
    public void onPause() {
        super.onPause();

        AlarmList.get(getActivity())
                .updateAlarm(mAlarm);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alarm, container, false);

        mTimePicker = (TimePicker) view.findViewById(R.id.alarm_time);
        mTimePicker.setCurrentHour(mAlarm.getTimeHour());
        mTimePicker.setCurrentMinute(mAlarm.getTimeMinute());

        mTitleField = (EditText) view.findViewById(R.id.alarm_title);
        mTitleField.setText(mAlarm.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (getActivity() == null) {
                    return;
                }
                mAlarm.setTitle(s.toString());
                updateAlarm();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        return view;
    }

    private void updateAlarm() {
        AlarmList.get(getActivity()).updateAlarm(mAlarm);
        mCallbacks.onAlarmUpdated(mAlarm);
    }
}
