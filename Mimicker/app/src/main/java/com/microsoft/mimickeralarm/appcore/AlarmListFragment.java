/*
 *
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license.
 *
 * Project Oxford: http://ProjectOxford.ai
 *
 * Project Oxford Mimicker Alarm Github:
 * https://github.com/Microsoft/ProjectOxford-Apps-MimickerAlarm
 *
 * Copyright (c) Microsoft Corporation
 * All rights reserved.
 *
 * MIT License:
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED ""AS IS"", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package com.microsoft.mimickeralarm.appcore;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
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
import android.widget.RelativeLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.mimickeralarm.R;
import com.microsoft.mimickeralarm.globalsettings.AlarmGlobalSettingsActivity;
import com.microsoft.mimickeralarm.model.Alarm;
import com.microsoft.mimickeralarm.model.AlarmList;
import com.microsoft.mimickeralarm.utilities.DateTimeUtilities;
import com.microsoft.mimickeralarm.utilities.Loggable;
import com.microsoft.mimickeralarm.utilities.Logger;

import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * This class holds the UI implementation of the Alarm list.  The alarm list consists of a
 * CoordinatorLayout at the root, with nested a AppBarLayout to hold the collapsing toolbar and a
 * RecyclerView to show the list of alarms.  If the RecyclerView is empty we swap it out for a
 * static layout which informs the user to click the FloatingActionButton to add a new alarm.
 *
 * This class contains two nested classes which are necessary for the implementation of the
 * RecyclerView:
 *      AlarmAdaptor - this is an implementation of RecyclerView.Adapter which manages the
 *      RecyclerView.ViewHolder classes.
 *
 *      AlarmHolder - this is an implementation of the RecyclerView.ViewHolder class, which
 *      initializes the list of alarm items.
 *
 * Some special features of this implementation:
 *      We treat the first item in the alarm list specially as we give it more height to avoid
 *      clashing with the the Floating Action Button.
 *
 *      We listen for visibility changes of the Floating Action Button as the header is expanded
 *      and collapsed. The visibility changes trigger the visibility of an "add alarm" button in
 *      the toolbar.
 *
 *      When the last item is removed from the alarm list we make the alarm header sticky so that
 *      it cannot be collapsed.
 */
public class AlarmListFragment extends Fragment implements
    AlarmFloatingActionButton.OnVisibilityChangedListener {

    public static final String ALARM_LIST_FRAGMENT_TAG = "alarm_list_fragment";

    private RecyclerView mAlarmRecyclerView;
    private RelativeLayout mEmptyView;
    private AlarmAdapter mAdapter;
    private CollapsingToolbarLayout mCollapsingLayout;
    private AppBarLayout mAppBarLayout;
    private AlarmListListener mCallbacks;
    private boolean mShowAddButtonInToolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Logger.init(getContext());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallbacks = (AlarmListListener) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alarm_list, container, false);

        mAlarmRecyclerView = (RecyclerView) view
                .findViewById(R.id.alarm_recycler_view);
        mAlarmRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(),
                DividerItemDecoration.VERTICAL_LIST));

        Toolbar toolbar = (Toolbar) view
                .findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.alarm_list_title);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);

        AlarmFloatingActionButton fab = (AlarmFloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addAlarm();
            }
        });
        fab.setVisibilityListener(this);

        mEmptyView = (RelativeLayout) view.findViewById(R.id.empty_view);

        mCollapsingLayout = (CollapsingToolbarLayout) view.findViewById(R.id.toolbar_layout);

        mAppBarLayout = (AppBarLayout) view.findViewById(R.id.app_bar);

        mAlarmRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        updateUI();

        ItemTouchHelper.Callback callback = new AlarmListItemTouchHelperCallback(mAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(mAlarmRecyclerView);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void visibilityChanged(int visibility) {
        if (View.GONE == visibility) {
            mShowAddButtonInToolbar = true;
        } else if (View.VISIBLE == visibility) {
            mShowAddButtonInToolbar = false;
        }
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_alarm_list, menu);
        MenuItem add = menu.findItem(R.id.action_add_alarm);
        add.setVisible(mShowAddButtonInToolbar);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            launchChildActivity(AlarmGlobalSettingsActivity.class);
            return true;
        } else if (id == R.id.action_learn_more) {
            launchChildActivity(LearnMoreActivity.class);
            return true;
        } else if (id == R.id.action_add_alarm) {
            addAlarm();
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

        if (alarms.isEmpty()) {
            mAlarmRecyclerView.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
            enableCollapsingBehaviour(false);
        } else {
            mAlarmRecyclerView.setVisibility(View.VISIBLE);
            mEmptyView.setVisibility(View.GONE);
            enableCollapsingBehaviour(true);
        }
    }

    private void addAlarm() {
        Alarm alarm = new Alarm();
        alarm.setNew(true);
        AlarmList.get(getActivity()).addAlarm(alarm);
        mCallbacks.onAlarmSelected(alarm);
    }

    private void launchChildActivity(Class childClass) {
        Intent intent = new Intent(getActivity(), childClass);
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void enableCollapsingBehaviour(boolean enableCollapse) {
        if (!enableCollapse) {
            mAppBarLayout.setExpanded(true);
        }
        AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) mCollapsingLayout.getLayoutParams();
        int scrollFlags = enableCollapse ?
                            AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED | AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL :
                            AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP;
        params.setScrollFlags(scrollFlags);
        mCollapsingLayout.setLayoutParams(params);
    }

    public interface AlarmListListener {
        void onAlarmSelected(Alarm alarm);
        void onAlarmChanged();
    }

    private class AlarmHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private TextView mTitleTextView;
        private TextView mTimeTextView;
        private SwitchCompat mAlarmEnabled;
        private RelativeLayout mContainer;

        private Alarm mAlarm;

        public AlarmHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            mTitleTextView = (TextView) itemView.findViewById(R.id.list_item_alarm_title_text_view);
            mTimeTextView = (TextView) itemView.findViewById(R.id.list_item_alarm_time_text_view);
            mAlarmEnabled = (SwitchCompat) itemView.findViewById(R.id.list_item_alarm_enabled_switch);
            mContainer = (RelativeLayout) itemView.findViewById(R.id.list_item_container);

            mAlarmEnabled.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mAlarmEnabled.isChecked()) {
                        long alarmTime = mAlarm.schedule();
                        Toast.makeText(getActivity(),
                                DateTimeUtilities.getTimeUntilAlarmDisplayString(getActivity(), alarmTime),
                                Toast.LENGTH_LONG)
                                .show();
                    } else {
                        mAlarm.cancel();
                    }
                    mCallbacks.onAlarmChanged();
                }
            });
        }

        public void bindAlarm(Alarm alarm) {
            mAlarm = alarm;

            String title = getTitle(alarm);
            if (title == null || title.isEmpty()) {
                mTitleTextView.setVisibility(View.GONE);
            } else {
                mTitleTextView.setVisibility(View.VISIBLE);
                mTitleTextView.setText(title);
            }

            mTimeTextView.setText(DateTimeUtilities.getUserTimeString(getContext(), mAlarm.getTimeHour(), mAlarm.getTimeMinute()));
            mAlarmEnabled.setChecked(mAlarm.isEnabled());
        }

        public void setFirstItemDimensions() {
            int itemHeightTall = getContext().getResources().getDimensionPixelSize(R.dimen.alarm_list_item_height_tall);
            TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, itemHeightTall);
            mContainer.setLayoutParams(params);

            int tallItemPadding = itemHeightTall - getContext().getResources().getDimensionPixelSize(R.dimen.alarm_list_item_height);
            mContainer.setPadding(0, tallItemPadding, 0, 0);
        }

        @Override
        public void onClick(View v) {
            mCallbacks.onAlarmSelected(mAlarm);
        }

        private String getTitle(Alarm alarm) {
            String alarmTitle = mAlarm.getTitle();
            if (alarm.isOneShot()) {
                return alarmTitle;
            } else {
                String summary = getDayPeriodSummary(alarm);
                if (alarmTitle == null || alarmTitle.isEmpty()) {
                    return summary;
                } else {
                    return alarmTitle + ", " + summary;
                }
            }
        }

        private String getDayPeriodSummary(Alarm alarm) {
            List<Integer> days = new ArrayList<>();
            for (int dayOfWeek = Calendar.SUNDAY; dayOfWeek <= Calendar.SATURDAY; dayOfWeek++) {
                if (alarm.getRepeatingDay(dayOfWeek - 1)) {
                    days.add(dayOfWeek);
                }
            }
            Integer[] daysWrapper = days.toArray(new Integer[days.size()]);
            int[] daysOfWeek =  ArrayUtils.toPrimitive(daysWrapper);
            return DateTimeUtilities.getDayPeriodSummaryString(getContext(), daysOfWeek);
        }
    }

    private class AlarmAdapter extends RecyclerView.Adapter<AlarmHolder>
        implements AlarmListItemTouchHelperCallback.ItemTouchHelperAdapter {

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
            if (position == 0) {
                holder.setFirstItemDimensions();
            }
            holder.bindAlarm(alarm);
        }

        @Override
        public int getItemCount() {
            return mAlarms.size();
        }

        public void setAlarms(List<Alarm> alarms) {
            mAlarms = alarms;
        }

        @Override
        public void onItemDismiss(int position) {
            Alarm alarm = mAlarms.remove(position);

            Loggable.UserAction userAction = new Loggable.UserAction(Loggable.Key.ACTION_ALARM_DELETE);
            userAction.putJSON(alarm.toJSON());
            Logger.track(userAction);

            alarm.delete();

            notifyItemRemoved(position);

            // If we are down to the last item, ensure we show the empty list graphic
            if (getItemCount() == 0) {
                updateUI();
            }

            mCallbacks.onAlarmChanged();
        }

        @Override
        public void onItemDismissCancel(int position) {
            notifyItemChanged(position);
        }
    }
}

