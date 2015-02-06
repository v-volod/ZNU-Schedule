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
import ua.zp.rozklad.app.util.CalendarUtils;

public class ScheduleOfWeekFragment extends Fragment {

    private static final String ARG_GROUP_ID = "groupId";
    private static final String ARG_SUBGROUP_ID = "subgroupId";
    private static final String ARG_START_OF_WEEK = "startOfWeek";
    private static final String ARG_PERIODICITY = "periodicity";

    private int groupId;
    private int subgroupId;
    private long startOfWeek;
    private int periodicity;

    private FragmentStatePagerAdapter mAdapter;
    private ViewPager mPager;
    private SlidingTabLayout mTabs;

    private final Handler handler = new Handler();
    private Runnable runPager;
    private boolean mCreated = false;

    public static ScheduleOfWeekFragment newInstance(
            int groupId, int subgroupId, long startOfWeek, int periodicity) {
        ScheduleOfWeekFragment fragment = new ScheduleOfWeekFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_GROUP_ID, groupId);
        args.putInt(ARG_SUBGROUP_ID, subgroupId);
        args.putLong(ARG_START_OF_WEEK, startOfWeek);
        args.putInt(ARG_PERIODICITY, periodicity);
        fragment.setArguments(args);
        return fragment;
    }

    public ScheduleOfWeekFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startOfWeek = CalendarUtils.getCurrentWeekInMillis();
        Bundle args = getArguments();
        if (args != null) {
            groupId = args.getInt(ARG_GROUP_ID);
            subgroupId = args.getInt(ARG_SUBGROUP_ID);
            startOfWeek = args.getLong(ARG_START_OF_WEEK);
            periodicity = args.getInt(ARG_PERIODICITY);
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

        fetchDays.start();

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

    public Thread fetchDays = new Thread() {
        @Override
        public void run() {
            ArrayList<Integer> days = new ArrayList<>();
            days.add(0);
            days.add(1);
            days.add(2);
            days.add(3);
            days.add(4);
            // TODO: Fetch data from DB.
            setAdapter(new DayPagerAdapter(getFragmentManager(), days));
        }
    };

    private class DayPagerAdapter extends FragmentStatePagerAdapter {

        private final String[] DAYS = getResources().getStringArray(R.array.days_of_week);

        private ArrayList<Integer> days;

        public DayPagerAdapter(FragmentManager fm, ArrayList<Integer> days) {
            super(fm);
            this.days = days;
        }

        @Override
        public Fragment getItem(int position) {
            return ScheduleFragment.newInstance(
                    groupId, subgroupId, startOfWeek, days.get(position), periodicity
            );
        }

        @Override
        public int getCount() {
            return days.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return DAYS[days.get(position)];
        }
    }
}
