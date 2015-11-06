package com.microsoft.smartalarm;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TimePicker;

public class TimePickerFragment extends DialogFragment {
    public static final String EXTRA_TIME_HOUR =
            "com.microsoft.smartalarm.time.hour";
    public static final String EXTRA_TIME_MINUTE =
            "com.microsoft.smartalarm.minute";

    private static final String ARG_TIME_HOUR = "time.hour";
    private static final String ARG_TIME_MINUTE = "time.minute";

    private TimePicker mTimePicker;

    public static TimePickerFragment newInstance(int hour, int minute) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_TIME_HOUR, hour);
        args.putSerializable(ARG_TIME_MINUTE, minute);

        TimePickerFragment fragment = new TimePickerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int hour = (int) getArguments().getSerializable(ARG_TIME_HOUR);
        int minute = (int) getArguments().getSerializable(ARG_TIME_MINUTE);

        View view = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_time, null);

        mTimePicker = (TimePicker) view.findViewById(R.id.dialog_time_time_picker);
        mTimePicker.setCurrentHour(hour);
        mTimePicker.setCurrentHour(minute);

        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int hour = mTimePicker.getCurrentHour();
                                int minute = mTimePicker.getCurrentMinute();

                                Intent intent = new Intent();
                                intent.putExtra(EXTRA_TIME_HOUR, hour);
                                intent.putExtra(EXTRA_TIME_MINUTE, minute);
                                getTargetFragment()
                                        .onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
                            }
                        })
                .create();
    }
}
