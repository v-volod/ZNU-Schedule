package ua.zp.rozklad.app.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ua.zp.rozklad.app.R;
import ua.zp.rozklad.app.adapter.CursorFragmentStatePagerAdapter;
import ua.zp.rozklad.app.ui.tabs.SlidingTabLayout;

import static java.lang.String.valueOf;
import static ua.zp.rozklad.app.provider.ScheduleContract.FullSchedule;
import static ua.zp.rozklad.app.provider.ScheduleContract.FullSchedule.Summary.Selection;
import static ua.zp.rozklad.app.provider.ScheduleContract.FullSchedule.Summary.SortOrder;
import static ua.zp.rozklad.app.provider.ScheduleContract.combineSelection;
import static ua.zp.rozklad.app.provider.ScheduleContract.groupBySelection;
import static ua.zp.rozklad.app.util.CalendarUtils.addWeeks;
import static ua.zp.rozklad.app.util.CalendarUtils.getCurrentWeekInMillis;

public class ScheduleOfWeekFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String ARG_GROUP_ID = "groupId";
    private static final String ARG_SUBGROUP_ID = "subgroupId";
    private static final String ARG_START_OF_WEEK = "startOfWeek";
    private static final String ARG_PERIODICITY = "periodicity";

    private int groupId;
    private int subgroupId;
    private long startOfWeek;
    private int periodicity;

    private DayPagerAdapter mAdapter;
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
        startOfWeek = getCurrentWeekInMillis();
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

        setAdapter(new DayPagerAdapter(getFragmentManager(), null));

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (runPager != null) {
            handler.post(runPager);
        }
        mCreated = true;
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onStart() {
        super.onStart();
        getLoaderManager().restartLoader(0, null, this);
    }

    public void changePeriodicity(int periodicity) {
        this.periodicity = periodicity;
        startOfWeek = addWeeks(getCurrentWeekInMillis(), periodicity - 1);
        getLoaderManager().restartLoader(0, null, this);
    }

    /**
     * @see android.support.v4.app.Fragment#onPause()
     */
    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(runPager);
    }

    protected void setAdapter(DayPagerAdapter adapter) {
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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader = new CursorLoader(getActivity());

        loader.setUri(FullSchedule.CONTENT_URI);
        loader.setProjection(new String[]{FullSchedule.DAY_OF_WEEK});
        loader.setSelection(
                combineSelection(Selection.GROUP, Selection.SUBGROUP, Selection.PERIODICITY) +
                        groupBySelection(FullSchedule.DAY_OF_WEEK)
        );
        loader.setSelectionArgs(new String[]{
                valueOf(groupId),
                valueOf(subgroupId),
                valueOf(periodicity)
        });
        loader.setSortOrder(SortOrder.DAY_OF_WEEK);

        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.getCount() == 0) {
            // TODO: Show "No schedule".
        }
        mAdapter.swapCursor(data);
        setAdapter(mAdapter);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private class DayPagerAdapter extends CursorFragmentStatePagerAdapter {

        private final String[] DAYS = getResources().getStringArray(R.array.days_of_week);

        public DayPagerAdapter(FragmentManager fm, Cursor cursor) {
            super(fm, cursor);
        }

        @Override
        public Fragment getItem(int position, Cursor cursor) {
            return ScheduleFragment.newInstance(
                    groupId, subgroupId, startOfWeek, cursor.getInt(0), periodicity
            );
        }

        @Override
        public CharSequence getPageTitle(int position, Cursor cursor) {
            return DAYS[cursor.getInt(0)];
        }
    }
}
