package ua.zp.rozklad.app.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import ua.zp.rozklad.app.R;
import ua.zp.rozklad.app.ui.tabs.SlidingTabLayout;

public class ScheduleOfWeekFragment extends Fragment {

    private FragmentStatePagerAdapter mAdapter;
    private ViewPager mPager;
    private SlidingTabLayout mTabs;

    private final Handler handler = new Handler();
    private Runnable runPager;
    private boolean mCreated = false;

    public static ScheduleOfWeekFragment newInstance() {
        ScheduleOfWeekFragment fragment = new ScheduleOfWeekFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public ScheduleOfWeekFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_schedule_of_week, container, false);

        mPager = (ViewPager) view.findViewById(R.id.pager);

        mTabs = (SlidingTabLayout) view.findViewById(R.id.tabs);
        mTabs.setCustomTabView(R.layout.tab_indicator, android.R.id.text1);
        mTabs.setSelectedIndicatorColors(getResources().getColor(R.color.colorPrimaryDark));
        mTabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.colorAccent);
            }
        });
        setAdapter(new DayPagerAdapter(getFragmentManager()));

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (runPager != null) {
            handler.post(runPager);
        }
        mCreated = true;
    }

    /**
     * @see android.support.v4.app.Fragment#onPause()
     */
    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(runPager);
    }

    protected void setAdapter(FragmentStatePagerAdapter adapter) {
        mAdapter = adapter;
        runPager = new Runnable() {
            @Override
            public void run() {
                mPager.setAdapter(mAdapter);
                mTabs.setViewPager(mPager);
            }
        };
        if (mCreated) {
            handler.post(runPager);
        }
    }

    private class DayPagerAdapter extends FragmentStatePagerAdapter {

        ArrayList<String> days;

        public DayPagerAdapter(FragmentManager fm) {
            super(fm);
            days = new ArrayList<>();
            for (String s : getResources().getStringArray(R.array.days_of_week))
                days.add(s);
        }

        @Override
        public Fragment getItem(int position) {
            return ScheduleFragment.newInstance(0, 0, 0, 0);
        }

        @Override
        public int getCount() {
            return days.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return days.get(position);
        }
    }
}
