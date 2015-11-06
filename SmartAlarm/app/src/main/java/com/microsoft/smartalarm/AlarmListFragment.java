package com.microsoft.smartalarm;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.List;

public class AlarmListFragment extends Fragment {

    private RecyclerView mAlarmRecyclerView;
    private AlarmAdapter mAdapter;
    private Toolbar mToolbar;
    private FloatingActionButton mFab;
    private Callbacks mCallbacks;

    public interface Callbacks {
        void onAlarmSelected(Alarm alarm);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallbacks = (Callbacks) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alarm_list, container, false);

        mAlarmRecyclerView = (RecyclerView) view
                .findViewById(R.id.alarm_recycler_view);

        mToolbar = (Toolbar) view
                .findViewById(R.id.toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(mToolbar);

        mFab = (FloatingActionButton) view.findViewById(R.id.fab);

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Alarm alarm = new Alarm();
                AlarmList.get(getActivity()).addAlarm(alarm);
                updateUI();
                mCallbacks.onAlarmSelected(alarm);
            }
        });

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT ) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                mAdapter.removeItem(viewHolder.getAdapterPosition());
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(mAlarmRecyclerView);

        mAlarmRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        updateUI();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_alarm_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void updateUI() {
        AlarmList alarmList = AlarmList.get(getActivity());
        List<Alarm> alarms = alarmList.getAlarms();

        if (mAdapter == null) {
            mAdapter = new AlarmAdapter(alarms);
            mAlarmRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setAlarms(alarms);
            mAdapter.notifyDataSetChanged();
        }
    }

    private class AlarmHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private TextView mTitleTextView;
        private TextView mTimeTextView;
        private SwitchCompat mAlarmEnabled;

        private Alarm mAlarm;

        public AlarmHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            mTitleTextView = (TextView) itemView.findViewById(R.id.list_item_alarm_title_text_view);
            mTimeTextView = (TextView) itemView.findViewById(R.id.list_item_alarm_time_text_view);
            mAlarmEnabled = (SwitchCompat) itemView.findViewById(R.id.list_item_alarm_enabled_switch);

            mAlarmEnabled.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     mAlarm.setIsEnabled(mAlarmEnabled.isChecked());
                     AlarmList.get(getActivity()).updateAlarm(mAlarm);
                 }
            });
        }

        public void bindAlarm(Alarm alarm) {
            mAlarm = alarm;

            String title = mAlarm.getTitle();
            if (title == null || title.isEmpty()){
                mTitleTextView.setText(getString(R.string.alarm_name_default));
            } else {
                mTitleTextView.setText(mAlarm.getTitle());
            }

            int hour = mAlarm.getTimeHour();
            int minute = mAlarm.getTimeMinute();
            mTimeTextView.setText(String.format("%tI %tM", (long)hour, (long)minute));
            mAlarmEnabled.setChecked(mAlarm.isEnabled());
        }

        @Override
        public void onClick(View v) {
            mCallbacks.onAlarmSelected(mAlarm);
        }
    }

    private class AlarmAdapter extends RecyclerView.Adapter<AlarmHolder> {

        private List<Alarm> mAlarms;

        public AlarmAdapter(List<Alarm> alarms) {
            mAlarms = alarms;
        }

        @Override
        public AlarmHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.list_item_alarm, parent, false);
            return new AlarmHolder(view);
        }

        @Override
        public void onBindViewHolder(AlarmHolder holder, int position) {
            Alarm alarm = mAlarms.get(position);
            holder.bindAlarm(alarm);
        }

        @Override
        public int getItemCount() {
            return mAlarms.size();
        }

        public void setAlarms(List<Alarm> alarms) {
            mAlarms = alarms;
        }

        public void removeItem(int position) {
            AlarmList.get(getActivity()).deleteAlarm(mAlarms.get(position));
            notifyItemRemoved(position);
        }
    }
}
