package com.microsoft.mimicker.ringing;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.microsoft.mimicker.R;
import com.microsoft.mimicker.model.Alarm;
import com.microsoft.mimicker.model.AlarmList;

import java.util.UUID;

public class AlarmNoGamesFragment extends Fragment {
    private static final String ARGS_ALARM_ID = "alarm_id";
    private static final int NOGAME_SCREEN_TIMEOUT_DURATION = 5 * 1000;
    NoGameResultListener mCallback;
    private Handler mHandler;
    private Runnable mAutoDismissTask;

    public static AlarmNoGamesFragment newInstance(String alarmId) {
        AlarmNoGamesFragment fragment = new AlarmNoGamesFragment();
        Bundle bundle = new Bundle(1);
        bundle.putString(ARGS_ALARM_ID, alarmId);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_no_mimics, container, false);

        Bundle args = getArguments();
        UUID alarmId = UUID.fromString(args.getString(ARGS_ALARM_ID));
        Alarm alarm = AlarmList.get(getContext()).getAlarm(alarmId);

        TextView alarmTitle = (TextView) view.findViewById(R.id.alarm_no_mimics_label);

        String name = alarm.getTitle();
        if (name == null || name.isEmpty()) {
            name = getString(R.string.alarm_ringing_default_text);
        }

        alarmTitle.setText(name);

        view.findViewById(R.id.alarm_no_mimics_tap_to_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandler.removeCallbacks(mAutoDismissTask);
                mCallback.onNoGameDismiss(true);
            }
        });

        mAutoDismissTask = new Runnable() {
            @Override
            public void run() {
                mCallback.onNoGameDismiss(false);
            }
        };

        mHandler = new Handler();
        mHandler.postDelayed(mAutoDismissTask, NOGAME_SCREEN_TIMEOUT_DURATION);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallback = (NoGameResultListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    public interface NoGameResultListener {
        void onNoGameDismiss(boolean launchSettings);
    }
}
