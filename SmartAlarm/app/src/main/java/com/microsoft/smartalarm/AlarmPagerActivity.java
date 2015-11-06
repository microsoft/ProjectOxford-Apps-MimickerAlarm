package com.microsoft.smartalarm;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import java.util.List;
import java.util.UUID;

public class AlarmPagerActivity extends AppCompatActivity
    implements AlarmFragment.Callbacks {

    private static final String EXTRA_ALARM_ID = "com.microsoft.smartalarm.alarm_id";

    private ViewPager mViewPager;
    private List<Alarm> mAlarms;

    public static Intent newIntent(Context packageContext, UUID alarmId) {
        Intent intent = new Intent(packageContext, AlarmPagerActivity.class);
        intent.putExtra(EXTRA_ALARM_ID, alarmId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_pager);

        final UUID alarmId = (UUID) getIntent()
                .getSerializableExtra(EXTRA_ALARM_ID);

        mViewPager = (ViewPager) findViewById(R.id.activity_alarm_pager_view_pager);

        mAlarms = AlarmList.get(this).getAlarms();
        FragmentManager fragmentManager = getSupportFragmentManager();
        mViewPager.setAdapter(new FragmentStatePagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int position) {
                Alarm alarm = mAlarms.get(position);
                return AlarmFragment.newInstance(alarm.getId());
            }

            @Override
            public int getCount() {
                return mAlarms.size();
            }
        });

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                Alarm alarm = mAlarms.get(position);
                if (alarm.getTitle() != null) {
                    setTitle(alarm.getTitle());
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }
        });

        for (int i = 0; i < mAlarms.size(); i++) {
            if (mAlarms.get(i).getId().equals(alarmId)) {
                mViewPager.setCurrentItem(i);

                break;
            }
        }
    }

    @Override
    public void onAlarmUpdated(Alarm alarm) {

    }
}
