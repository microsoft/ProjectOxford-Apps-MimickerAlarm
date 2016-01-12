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

public class AlarmNoMimicsFragment extends Fragment {
    public static final String NO_MIMICS_FRAGMENT_TAG = "no_mimics_fragment";
    private static final String ARGS_ALARM_ID = "alarm_id";
    private static final int NOGAME_SCREEN_TIMEOUT_DURATION = 5 * 1000;
    NoMimicResultListener mCallback;
    private Handler mHandler;
    private Runnable mAutoDismissTask;

    public static AlarmNoMimicsFragment newInstance(String alarmId) {
        AlarmNoMimicsFragment fragment = new AlarmNoMimicsFragment();
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
        alarmTitle.setText(name);

        view.findViewById(R.id.alarm_no_mimics_tap_to_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandler.removeCallbacks(mAutoDismissTask);
                mCallback.onNoMimicDismiss(true);
            }
        });

        mAutoDismissTask = new Runnable() {
            @Override
            public void run() {
                mCallback.onNoMimicDismiss(false);
            }
        };
        mHandler = new Handler();
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallback = (NoMimicResultListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    @Override
    public void onPause() {
        mHandler.removeCallbacks(mAutoDismissTask);
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mHandler.postDelayed(mAutoDismissTask, NOGAME_SCREEN_TIMEOUT_DURATION);
    }

    public interface NoMimicResultListener {
        void onNoMimicDismiss(boolean launchSettings);
    }
}
