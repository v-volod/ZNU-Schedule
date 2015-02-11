package ua.zp.rozklad.app.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.melnykov.fab.FloatingActionButton;

import ua.zp.rozklad.app.App;
import ua.zp.rozklad.app.R;
import ua.zp.rozklad.app.adapter.CursorFragmentStatePagerAdapter;
import ua.zp.rozklad.app.model.Periodicity;
import ua.zp.rozklad.app.ui.tabs.SlidingTabLayout;

import static java.lang.Math.abs;
import static java.lang.String.valueOf;
import static ua.zp.rozklad.app.provider.ScheduleContract.FullSchedule;
import static ua.zp.rozklad.app.provider.ScheduleContract.FullSchedule.Summary.Selection;
import static ua.zp.rozklad.app.provider.ScheduleContract.FullSchedule.Summary.SortOrder;
import static ua.zp.rozklad.app.provider.ScheduleContract.combineSelection;
import static ua.zp.rozklad.app.provider.ScheduleContract.combineSortOrder;
import static ua.zp.rozklad.app.provider.ScheduleContract.groupBySelection;
import static ua.zp.rozklad.app.util.CalendarUtils.getCurrentDayOfWeek;
import static ua.zp.rozklad.app.util.CalendarUtils.getCurrentTimeOfDayMillis;
import static ua.zp.rozklad.app.util.CalendarUtils.getCurrentWeekOfYear;
import static ua.zp.rozklad.app.util.CalendarUtils.getStartOfWeekMillis;

public class ScheduleOfWeekFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOADER_SCHEDULE_OF_WEEK_1 = 0;

    private static final String ARG_GROUP_ID = "groupId";
    private static final String ARG_SUBGROUP_ID = "subgroupId";
    private static final String ARG_SELECTED_DAY_POSITION = "selectedDayPosition";
    private static final String ARG_SELECTED_WEEK_POSITION = "selectedWeekPosition";
    private static final int[] weeks = new int[2];

    private Periodicity periodicity;

    private int currentDayPosition;
    private int selectedDayPosition;
    private int selectedWeekPosition;

    private int groupId;
    private int subgroupId;

    private OnPeriodicityChangeListener mListener;

    private DayPagerAdapter mAdapter;
    private ViewPager mPager;
    private SlidingTabLayout mTabs;
    private FloatingActionButton mFab;

    public static ScheduleOfWeekFragment newInstance(int groupId, int subgroupId) {
        ScheduleOfWeekFragment fragment = new ScheduleOfWeekFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_GROUP_ID, groupId);
        args.putInt(ARG_SUBGROUP_ID, subgroupId);
        fragment.setArguments(args);
        return fragment;
    }

    public ScheduleOfWeekFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnPeriodicityChangeListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnPeriodicityChangeListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            groupId = args.getInt(ARG_GROUP_ID);
            subgroupId = args.getInt(ARG_SUBGROUP_ID);
        }
        weeks[0] = getCurrentWeekOfYear();
        weeks[1] = weeks[0] + 1;
        periodicity = App.getInstance().getPreferencesUtils().getPeriodicity();

        selectedWeekPosition = 0;
        selectedDayPosition = -1;
        currentDayPosition = -1;

        if (savedInstanceState != null) {
            selectedWeekPosition = savedInstanceState.getInt(ARG_SELECTED_WEEK_POSITION, 0);
            selectedDayPosition = savedInstanceState.getInt(ARG_SELECTED_DAY_POSITION, -1);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(ARG_SELECTED_DAY_POSITION, selectedDayPosition);
        outState.putInt(ARG_SELECTED_WEEK_POSITION, selectedWeekPosition);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_schedule_of_week, container, false);

        mPager = (ViewPager) view.findViewById(R.id.pager);
        mPager.setOnPageChangeListener(onPageChangeListener);

        mTabs = (SlidingTabLayout) view.findViewById(R.id.tabs);
        mTabs.setCustomTabView(R.layout.tab_indicator, android.R.id.text1);
        mTabs.setSelectedIndicatorColors(getResources().getColor(R.color.colorPrimaryDark));
        mTabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.colorAccent);
            }
        });
        mTabs.setOnPageChangeListener(onPageChangeListener);

        mFab = (FloatingActionButton) view.findViewById(R.id.fab);
        mFab.setOnClickListener(togglePeriodicityListener);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAdapter = new DayPagerAdapter(getFragmentManager(), null);
        mPager.setAdapter(mAdapter);
        mTabs.setViewPager(mPager);
        getLoaderManager().initLoader(LOADER_SCHEDULE_OF_WEEK_1, null, this);
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
                valueOf(periodicity.getPeriodicity(weeks[selectedWeekPosition]))
        });
        loader.setSortOrder(combineSortOrder(SortOrder.DAY_OF_WEEK, SortOrder.END_TIME_DESC));

        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.getCount() > 0) {
            /*
            * Check if day is not selected before.
            * */

            if (selectedDayPosition == -1) {
                /*
                * Find position of the current day.
                * */
                currentDayPosition = findCurrentDayPosition(data);

                if (currentDayPosition == -1) {
                    selectedWeekPosition = abs(selectedWeekPosition - 1);
                    selectedDayPosition = 0;
                    getLoaderManager().restartLoader(LOADER_SCHEDULE_OF_WEEK_1, null, this);
                    return;
                }

                selectedDayPosition = currentDayPosition;
            }

            mListener.onPeriodicityChanged(
                    periodicity.getPeriodicity(weeks[selectedWeekPosition])
            );
        } else {
            onPeriodicityChanged(0);
        }

        mAdapter.swapCursor(data);
        mTabs.setViewPager(mPager);
        mPager.setCurrentItem(selectedDayPosition, true);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (!isRemoving()) {
            mAdapter.swapCursor(null);
            onPeriodicityChanged(0);
        }
    }

    private void onPeriodicityChanged(int periodicity) {
        if (mListener != null) {
            mListener.onPeriodicityChanged(periodicity);
        }
    }

    private int findCurrentDayPosition(Cursor cursor) {
        int currentDay = getCurrentDayOfWeek();
        int position = 0;
        int day;

        if (cursor.moveToFirst()) {
            do {
                // Get day of the schedule item.
                day = cursor.getInt(0);

                // Found current day.
                if (currentDay == day) {
                    /*
                    * Time of the current day higher than the time of the time of the last schedule
                    * item of the day
                    * */
                    if (getCurrentTimeOfDayMillis() > cursor.getLong(1)) {
                        /*
                        * If the day is the last day in the schedule, return undefined day position.
                        * */
                        if (position == cursor.getCount() - 1) {
                            return -1;
                        }
                        // Return next day position.
                        return position + 1;
                    }
                    // Return current day position.
                    return position;
                }

                // If the day is higher than the current day.
                if (currentDay < day) {
                    // Return higher day position.
                    return position;
                }
                position++;
            } while (cursor.moveToNext());
        }

        // Return undefined day position.
        return -1;
    }

    private ViewPager.SimpleOnPageChangeListener
            onPageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            selectedDayPosition = position;
            ((ScheduleFragment) mAdapter.instantiateItem(mPager, position)).attachFAB(mFab);
        }
    };

    private View.OnClickListener togglePeriodicityListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            selectedWeekPosition = abs(selectedWeekPosition - 1);
            getLoaderManager()
                    .restartLoader(LOADER_SCHEDULE_OF_WEEK_1, null, ScheduleOfWeekFragment.this);
        }
    };

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

        public DayPagerAdapter(FragmentManager fm, Cursor cursor) {
            super(fm, cursor);

        }

        @Override
        public ScheduleFragment getItem(int position, Cursor cursor) {
            int week = weeks[selectedWeekPosition];
            int day = cursor.getInt(0);

            return ScheduleFragment.newInstance(
                    position,
                    week == getCurrentWeekOfYear() && day == getCurrentDayOfWeek(),
                    groupId,
                    subgroupId,
                    getStartOfWeekMillis(week),
                    day,
                    periodicity.getPeriodicity(week)
            );
        }

        @Override
        public int getItemPosition(Object object) {
            ScheduleFragment fragment = (ScheduleFragment) object;

            int oldPosition = fragment.getPosition();

            Cursor cursor = getCursor();
            if (cursor != null && cursor.moveToPosition(oldPosition)) {
                int week = weeks[selectedWeekPosition];
                int day = fragment.getDayOfWeek();

                if (day == cursor.getInt(0)) {
                    int curPeriodicity = periodicity.getPeriodicity(week);
                    boolean isToday = week == getCurrentWeekOfYear() && day == getCurrentDayOfWeek();

                    if (curPeriodicity != fragment.getPeriodicity()) {
                        fragment.reload(isToday, getStartOfWeekMillis(week), curPeriodicity);
                    }

                    return POSITION_UNCHANGED;
                }
            }

            return POSITION_NONE;
        }

        @Override
        public CharSequence getPageTitle(int position, Cursor cursor) {
            return DAYS[cursor.getInt(0)];
        }
    }
}
