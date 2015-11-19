package com.microsoft.smartalarm;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import java.sql.Time;
import java.util.UUID;

public class AlarmFragment extends Fragment {
    private static final String ARG_ALARM_ID = "alarm_id";

    private TimePicker mTimePicker;
    private EditText mTitleField;
    private TextView mToneField;
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
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallbacks = (Callbacks)context;
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
        mTimePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                mAlarm.setTimeHour(hourOfDay);
                mAlarm.setTimeMinute(minute);
                updateAlarm();
            }
        });

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

        mToneField = (TextView) view.findViewById(R.id.alarm_ringtone);
        mToneField.setText(RingtoneManager.getRingtone(getContext(), mAlarm.getAlarmTone()).getTitle(getContext()));

        final LinearLayout ringToneContainer = (LinearLayout) view.findViewById(R.id.alarm_ringtone_container);
        ringToneContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                startActivityForResult(intent, 1);
            }
        });
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case 1: {
                    Uri alarmTone = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                    mAlarm.setAlarmTone(alarmTone);
                    mToneField.setText(RingtoneManager.getRingtone(getContext(), mAlarm.getAlarmTone()).getTitle(getContext()));
                    updateAlarm();
                }
            }
        }
    }

    private void updateAlarm() {
        AlarmManagerHelper.cancelAlarms(getContext());
        AlarmList.get(getActivity()).updateAlarm(mAlarm);
        mCallbacks.onAlarmUpdated(mAlarm);
        AlarmManagerHelper.setAlarms(getContext());
    }
}
