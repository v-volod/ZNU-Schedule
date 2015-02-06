package ua.zp.rozklad.app.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.melnykov.fab.FloatingActionButton;

import ua.zp.rozklad.app.R;
import ua.zp.rozklad.app.adapter.CursorFragmentStatePagerAdapter;
import ua.zp.rozklad.app.ui.tabs.SlidingTabLayout;
import ua.zp.rozklad.app.util.CalendarUtils;

import static java.lang.Math.abs;
import static java.lang.String.valueOf;
import static java.lang.System.currentTimeMillis;
import static ua.zp.rozklad.app.provider.ScheduleContract.FullSchedule;
import static ua.zp.rozklad.app.provider.ScheduleContract.FullSchedule.Summary.Selection;
import static ua.zp.rozklad.app.provider.ScheduleContract.FullSchedule.Summary.SortOrder;
import static ua.zp.rozklad.app.provider.ScheduleContract.combineSelection;
import static ua.zp.rozklad.app.provider.ScheduleContract.combineSortOrder;
import static ua.zp.rozklad.app.provider.ScheduleContract.groupBySelection;
import static ua.zp.rozklad.app.util.CalendarUtils.addDays;
import static ua.zp.rozklad.app.util.CalendarUtils.addWeeks;
import static ua.zp.rozklad.app.util.CalendarUtils.getCurrentDayStartInMillis;
import static ua.zp.rozklad.app.util.CalendarUtils.getCurrentWeekStartInMillis;
import static ua.zp.rozklad.app.util.CalendarUtils.getDayStartInMillis;

public class ScheduleOfWeekFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>, ViewPager.OnPageChangeListener,
        View.OnClickListener {

    private static final String ARG_GROUP_ID = "groupId";
    private static final String ARG_SUBGROUP_ID = "subgroupId";
    private static final String ARG_START_OF_WEEK = "startOfWeek";
    private static final String ARG_PERIODICITY = "periodicity";


    private int selectedDayItem = -1;
    private int groupId;
    private int subgroupId;
    private long startOfWeek;
    private int periodicity;

    private OnPeriodicityChangeListener mListener;

    private DayPagerAdapter mAdapter;
    private ViewPager mPager;
    private SlidingTabLayout mTabs;
    private FloatingActionButton mFab;

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
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (OnPeriodicityChangeListener) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startOfWeek = getCurrentWeekStartInMillis();
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
        mPager.setOnPageChangeListener(this);

        mTabs = (SlidingTabLayout) view.findViewById(R.id.tabs);
        mTabs.setCustomTabView(R.layout.tab_indicator, android.R.id.text1);
        mTabs.setSelectedIndicatorColors(getResources().getColor(R.color.colorPrimaryDark));
        mTabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.colorAccent);
            }
        });
        mTabs.setOnPageChangeListener(this);

        mFab = (FloatingActionButton) view.findViewById(R.id.fab);
        mFab.setOnClickListener(this);

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

    public void swapPeriodicity() {
        periodicity = abs(periodicity - 3);
        startOfWeek = addWeeks(getCurrentWeekStartInMillis(), periodicity - 1);
        getLoaderManager().restartLoader(0, null, this);
        mListener.onPeriodicityChanged(periodicity);
    }

    /**
     * @see android.support.v4.app.Fragment#onPause()
     */
    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(runPager);
    }

    protected void setAdapter(final DayPagerAdapter adapter) {
        mAdapter = adapter;
        runPager = new Runnable() {
            @Override
            public void run() {
                invalidate();
            }
        };
        if (mCreated) {
            handler.post(runPager);
        }
    }

    protected void invalidate() {
        mPager.setAdapter(mAdapter);
        mTabs.setViewPager(mPager);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader = new CursorLoader(getActivity());

        loader.setUri(FullSchedule.CONTENT_URI);
        loader.setProjection(new String[]{
                FullSchedule.DAY_OF_WEEK,
                FullSchedule.MAX_END_TIME
        });
        loader.setSelection(
                combineSelection(Selection.GROUP, Selection.SUBGROUP, Selection.PERIODICITY) +
                        groupBySelection(FullSchedule.DAY_OF_WEEK)
        );
        loader.setSelectionArgs(new String[]{
                valueOf(groupId),
                valueOf(subgroupId),
                valueOf(periodicity)
        });
        loader.setSortOrder(combineSortOrder(SortOrder.DAY_OF_WEEK, SortOrder.END_TIME_DESC));

        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.getCount() == 0) {
            // TODO: Show "No schedule".
        }
        mAdapter.swapCursor(data);
        invalidate();
        if (data.getCount() > 0) {
            if (selectedDayItem == -1) {
                int currentDay =
                        (int) ((currentTimeMillis() - startOfWeek) / CalendarUtils.DAY_TIME_STAMP);
                long time = (currentTimeMillis() - startOfWeek) % CalendarUtils.DAY_TIME_STAMP;

                if (data.moveToFirst() && currentDay < data.getCount()) {
                    data.move(currentDay);
                    if (time > data.getLong(1)) {
                        currentDay++;
                    }
                }
                selectedDayItem = mAdapter.findDayForSelection(currentDay);
            }
            mPager.setCurrentItem(selectedDayItem, true);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        selectedDayItem = position;
        mAdapter.updateFab();
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onClick(View v) {
        swapPeriodicity();
    }

    /**
     * Interface definition for a callback to be invoked when a periodicity changed.
     */
    public interface OnPeriodicityChangeListener {
        /**
         * @param periodicity id of the row of the schedule table in the database.
         */
        public void onPeriodicityChanged(int periodicity);
    }

    private class DayPagerAdapter extends CursorFragmentStatePagerAdapter {

        private final String[] DAYS = getResources().getStringArray(R.array.days_of_week);

        private SparseArray<ScheduleFragment> registeredFragments = new SparseArray<>();

        public DayPagerAdapter(FragmentManager fm, Cursor cursor) {
            super(fm, cursor);

        }

        @Override
        public ScheduleFragment getItem(int position, Cursor cursor) {
            ScheduleFragment fragment =
                    ScheduleFragment.newInstance(getCurrentDayStartInMillis() ==
                                    getDayStartInMillis(addDays(startOfWeek, cursor.getInt(0))),
                            groupId, subgroupId, startOfWeek, cursor.getInt(0), periodicity
                    );
            if (selectedDayItem == position) {
                fragment.attachFAB(mFab);
            }
            registeredFragments.put(position, fragment);
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            registeredFragments.remove(position);
            super.destroyItem(container, position, object);
        }

        @Override
        public CharSequence getPageTitle(int position, Cursor cursor) {
            return DAYS[cursor.getInt(0)];
        }

        public int findDayForSelection(int currentDay) {
            Cursor cursor = getCursor();

            int dayForSelection = 0;
            int day;

            if (cursor.moveToFirst()) {
                do {
                    day = cursor.getInt(0);
                    if (day == currentDay || day > dayForSelection) {
                        return dayForSelection;
                    }
                    dayForSelection++;
                } while (cursor.moveToNext());
            }

            return 0;
        }

        public void updateFab() {
            ScheduleFragment fragment = registeredFragments.get(selectedDayItem);
            if (fragment != null) {
                fragment.attachFAB(mFab);
            }
        }
    }
}
