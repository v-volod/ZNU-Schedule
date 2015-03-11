package ua.pp.rozkladznu.app.ui;

import android.accounts.Account;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;

import ua.pp.rozkladznu.app.App;
import ua.pp.rozkladznu.app.R;
import ua.pp.rozkladznu.app.account.GroupAccount;
import ua.pp.rozkladznu.app.adapter.CursorFragmentStatePagerAdapter;
import ua.pp.rozkladznu.app.model.Periodicity;
import ua.pp.rozkladznu.app.provider.ScheduleContract;
import ua.pp.rozkladznu.app.sync.ScheduleSyncAdapter;
import ua.pp.rozkladznu.app.ui.tabs.SlidingTabLayout;
import ua.pp.rozkladznu.app.util.CalendarUtils;
import ua.pp.rozkladznu.app.util.UiUtils;

import static java.lang.Math.abs;
import static ua.pp.rozkladznu.app.provider.ScheduleContract.FullSchedule;
import static ua.pp.rozkladznu.app.provider.ScheduleContract.FullSchedule.Summary.Selection;
import static ua.pp.rozkladznu.app.provider.ScheduleContract.FullSchedule.Summary.SortOrder;
import static ua.pp.rozkladznu.app.provider.ScheduleContract.combineArgs;
import static ua.pp.rozkladznu.app.provider.ScheduleContract.combineSelection;
import static ua.pp.rozkladznu.app.provider.ScheduleContract.combineSortOrder;
import static ua.pp.rozkladznu.app.provider.ScheduleContract.groupBySelection;
import static ua.pp.rozkladznu.app.util.CalendarUtils.getCurrentDayOfWeek;
import static ua.pp.rozkladznu.app.util.CalendarUtils.getCurrentTimeOfDayMillis;
import static ua.pp.rozkladznu.app.util.CalendarUtils.getCurrentWeekOfYear;
import static ua.pp.rozkladznu.app.util.CalendarUtils.getStartOfWeekMillis;

public class ScheduleOfWeekFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>, SwipeRefreshLayout.OnRefreshListener {

    private static final int CURRENT_APPROXIMATE_DAY = -1;
    private static final int CURRENT_CONVENIENT_DAY = -2;
    private static final int NEXT_WEEK_EMPTY = -3;

    private static final String ARG_SCHEDULE_TYPE = "scheduleType";
    private static final String ARG_TYPE_FILTER_ID = "typeFilterId";
    private static final String ARG_SUBGROUP_ID = "subgroupId";
    private static final String ARG_SELECTED_DAY_POSITION = "selectedDayPosition";
    private static final String ARG_SELECTED_WEEK_POSITION = "selectedWeekPosition";

    public static interface Type {
        int BY_GROUP = 0;
        int BY_LECTURER = 1;
    }

    private static final int[] REFRESH_VIEW_COLOR_RES = {
            R.color.red_500, R.color.blue_500, R.color.green_500, R.color.yellow_600
    };

    private static final IntentFilter SYNC_RECEIVER_INTENT_FILTER =
            new IntentFilter(ScheduleSyncAdapter.ACTION_SYNC_STATUS);

    private Periodicity periodicity;

    private int selectedDayPosition;
    private int selectedWeekPosition;

    private int scheduleType;
    private long typeFilterId;
    private int subgroupId;

    private static final int[] weeks = new int[2];

    private OnPeriodicityChangeListener mListener;

    private DayPagerAdapter mAdapter;
    private ViewPager mPager;
    private SlidingTabLayout mTabs;
    private FloatingActionButton mFab;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private boolean isAttached = false;

    private Handler mHandler = new Handler();

    public static ScheduleOfWeekFragment newInstance(GroupAccount groupAccount) {
        ScheduleOfWeekFragment fragment = new ScheduleOfWeekFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SCHEDULE_TYPE, Type.BY_GROUP);
        args.putLong(ARG_TYPE_FILTER_ID, groupAccount.getGroupId());
        args.putInt(ARG_SUBGROUP_ID, groupAccount.getSubgroup());
        fragment.setArguments(args);
        return fragment;
    }

    public static ScheduleOfWeekFragment newInstance(long lecturerId) {
        ScheduleOfWeekFragment fragment = new ScheduleOfWeekFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SCHEDULE_TYPE, Type.BY_LECTURER);
        args.putLong(ARG_TYPE_FILTER_ID, lecturerId);
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

        isAttached = true;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        isAttached = false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            scheduleType = args.getInt(ARG_SCHEDULE_TYPE);
            typeFilterId = args.getLong(ARG_TYPE_FILTER_ID);
            subgroupId = args.getInt(ARG_SUBGROUP_ID);
        }
        weeks[0] = getCurrentWeekOfYear();
        weeks[1] = weeks[0] + 1;
        periodicity = App.getInstance().getPreferencesUtils().getPeriodicity();

        selectedWeekPosition = 0;
        selectedDayPosition = CURRENT_CONVENIENT_DAY;

        if (savedInstanceState != null) {
            selectedWeekPosition = savedInstanceState.getInt(ARG_SELECTED_WEEK_POSITION, 0);
            selectedDayPosition = savedInstanceState
                    .getInt(ARG_SELECTED_DAY_POSITION, CURRENT_CONVENIENT_DAY);
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
        mTabs.setSelectedIndicatorColors(
                UiUtils.getThemeAttribute(getActivity(), R.attr.colorAccent).data
        );
        mTabs.setOnPageChangeListener(onPageChangeListener);

        mFab = (FloatingActionButton) view.findViewById(R.id.fab);
        mFab.setOnClickListener(togglePeriodicityListener);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(REFRESH_VIEW_COLOR_RES);
        mSwipeRefreshLayout.setEnabled(false);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAdapter = new DayPagerAdapter(getFragmentManager(), null);
        mPager.setAdapter(mAdapter);
        mTabs.setViewPager(mPager);
        getLoaderManager().initLoader(scheduleType, null, this);
    }

    @Override
    public void onStart() {
        super.onStart();
        mTabs.invalidate();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader = new CursorLoader(getActivity());

        // Monday
        long startDateMillis = getStartOfWeekMillis(weeks[selectedWeekPosition]);
        // Sunday
        long endDateMillis = CalendarUtils.addDays(startDateMillis, 5);

        loader.setUri(FullSchedule.CONTENT_URI);
        loader.setProjection(new String[]{
                FullSchedule.DAY_OF_WEEK,
                FullSchedule.MAX_END_TIME
        });
        switch (id) {
            case Type.BY_GROUP:
                loader.setSelection(combineSelection(
                                Selection.GROUP,
                                Selection.SUBGROUP,
                                Selection.AFTER_DATE,
                                Selection.BEFORE_DATE
                        ) + groupBySelection(FullSchedule.DAY_OF_WEEK)
                );
                loader.setSelectionArgs(combineArgs(
                        typeFilterId,
                        subgroupId,
                        startDateMillis,
                        endDateMillis
                ));
                break;
            case Type.BY_LECTURER:
                loader.setSelection(combineSelection(
                                Selection.LECTURER,
                                Selection.AFTER_DATE,
                                Selection.BEFORE_DATE
                        ) + groupBySelection(FullSchedule.DAY_OF_WEEK)
                );
                loader.setSelectionArgs(combineArgs(
                        typeFilterId,
                        startDateMillis,
                        endDateMillis
                ));
                break;
        }
        loader.setSortOrder(combineSortOrder(SortOrder.DAY_OF_WEEK, SortOrder.END_TIME_DESC));

        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        periodicity = App.getInstance().getPreferencesUtils().getPeriodicity();
        if (data.getCount() > 0) {
            if (mPager.getVisibility() == View.GONE) {
                mPager.setVisibility(View.VISIBLE);
            }

            /*
            * Check if day is not selected before.
            * */
            if (selectedDayPosition == CURRENT_APPROXIMATE_DAY) {
                selectedDayPosition = mAdapter.findCurrentDayPosition(data, false, false);
            } else if (selectedDayPosition == CURRENT_CONVENIENT_DAY) {
                /*
                * Find position of the current day.
                * */
                int currentDayPosition;
                if (scheduleType == Type.BY_GROUP) {
                    currentDayPosition = mAdapter.findCurrentDayPosition(data, true, true);
                } else {
                    currentDayPosition = mAdapter.findCurrentDayPosition(data, true, false);
                }

                if (currentDayPosition == DayPagerAdapter.DAY_POSITION_NON_THIS_WEEK) {
                    selectedDayPosition = 0;
                    toggleWeek();
                    return;
                }

                selectedDayPosition = currentDayPosition;
            } else if (selectedDayPosition == NEXT_WEEK_EMPTY) {
                selectedDayPosition = mAdapter.findCurrentDayPosition(data, true, false);
            }

        } else {
            selectedDayPosition = NEXT_WEEK_EMPTY;
            if (mPager.getVisibility() == View.VISIBLE) {
                mPager.setVisibility(View.GONE);
            }
        }

        onPeriodicityChanged(periodicity.getPeriodicity(weeks[selectedWeekPosition]));
        mAdapter.swapCursor(data);
        mTabs.setViewPager(mPager);
        mPager.setCurrentItem(selectedDayPosition, true);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (mPager.getVisibility() == View.VISIBLE) {
            mPager.setVisibility(View.GONE);
        }
        if (getActivity() != null && !getActivity().isFinishing() && !isRemoving()) {
            mAdapter.swapCursor(null);
            onPeriodicityChanged(0);
        }
    }

    public void toggleWeek() {
        selectedWeekPosition = abs(selectedWeekPosition - 1);
        getLoaderManager().restartLoader(scheduleType, null, this);
    }

    private void onPeriodicityChanged(int periodicity) {
        if (mListener != null) {
            mListener.onPeriodicityChanged(periodicity);
        }
    }


    public void setCurrentDayPage() {
        if (mAdapter.getCount() == 0) {
            return;
        }
        int currentDayPosition = mAdapter.findCurrentDayPosition();

        if (currentDayPosition == DayPagerAdapter.DAY_POSITION_UNDEFINED) {
            if (getCurrentWeekOfYear() == weeks[selectedWeekPosition]) {
                currentDayPosition = mAdapter.findCurrentDayPosition(true, true);

                if (selectedDayPosition != currentDayPosition) {
                    mPager.setCurrentItem(currentDayPosition);
                }
            } else {
                selectedDayPosition = CURRENT_CONVENIENT_DAY;
                toggleWeek();
            }

            Toast.makeText(getActivity(), R.string.day_off, Toast.LENGTH_SHORT).show();
        } else {
            if (getCurrentWeekOfYear() == weeks[selectedWeekPosition]) {
                if (selectedDayPosition != currentDayPosition) {
                    mPager.setCurrentItem(currentDayPosition);
                }

            } else {
                selectedDayPosition = CURRENT_APPROXIMATE_DAY;
                toggleWeek();
            }
        }
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
            toggleWeek();
        }
    };

    public int getScheduleType() {
        return scheduleType;
    }

    public void reload(GroupAccount groupAccount) {
        if (typeFilterId != groupAccount.getGroupId() || subgroupId != groupAccount.getSubgroup()) {
            typeFilterId = groupAccount.getGroupId();
            subgroupId = groupAccount.getSubgroup();
            Bundle args = getArguments();
            if (args != null) {
                args.putLong(ARG_TYPE_FILTER_ID, typeFilterId);
                args.putInt(ARG_SUBGROUP_ID, subgroupId);
            }
            if (isAttached) {
                mAdapter.changeCursor(null);
                getLoaderManager()
                        .restartLoader(scheduleType, null, this);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (scheduleType == Type.BY_GROUP) {
            getActivity().registerReceiver(mSyncBroadcastReceiver, SYNC_RECEIVER_INTENT_FILTER);

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    GroupAccount groupAccount = App.getInstance()
                            .getGroupAuthenticatorHelper().getActiveAccount();
                    if (groupAccount != null) {
                        boolean isSyncPending =
                                ContentResolver.isSyncPending(
                                        groupAccount.getBaseAccount(),
                                        ScheduleContract.CONTENT_AUTHORITY
                                );

                        if (App.getInstance().isManualSyncActive() ||
                                isSyncPending && App.getInstance().isManualSyncRequested()) {
                            showRefreshView();
                        }
                    }
                }
            }, 1000);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (scheduleType == Type.BY_GROUP) {
            getActivity().unregisterReceiver(mSyncBroadcastReceiver);
            hideRefreshView();
        }
    }

    private void showRefreshView() {
        mSwipeRefreshLayout.setRefreshing(true);
    }

    private void hideRefreshView() {
        mSwipeRefreshLayout.setRefreshing(false);
    }

    public void performSync() {
        GroupAccount groupAccount = App.getInstance().getGroupAuthenticatorHelper().getActiveAccount();

        if (groupAccount != null) {
            Account account = groupAccount.getBaseAccount();

            boolean isSyncActive =
                    ContentResolver.isSyncPending(account, ScheduleContract.CONTENT_AUTHORITY);
            boolean isSyncPending =
                    ContentResolver.isSyncActive(account, ScheduleContract.CONTENT_AUTHORITY);
            if (!isSyncActive && !isSyncPending) {
                Bundle bundle = new Bundle();
                bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
                bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);

                ContentResolver.requestSync(account, ScheduleContract.CONTENT_AUTHORITY, bundle);
            }

            mSwipeRefreshLayout.setRefreshing(true);
            App.getInstance().setManualSyncRequested(true);
        }
    }

    @Override
    public void onRefresh() {
        performSync();
    }

    private final BroadcastReceiver mSyncBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            int syncState = intent.getIntExtra(ScheduleSyncAdapter.EXTRA_SYNC_STATUS, -1);
            switch (syncState) {
                case ScheduleSyncAdapter.SyncStatus.START:
                    // Noop
                    break;
                case ScheduleSyncAdapter.SyncStatus.ABORTED_WITH_ERROR:
                case ScheduleSyncAdapter.SyncStatus.FINISHED:
                    hideRefreshView();
                    break;
            }
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

        private static final int DAY_POSITION_NON_THIS_WEEK = -1;
        private static final int DAY_POSITION_UNDEFINED = -2;

        private final String[] DAYS = getResources().getStringArray(R.array.days_of_week);

        public DayPagerAdapter(FragmentManager fm, Cursor cursor) {
            super(fm, cursor);

        }

        @Override
        public ScheduleFragment getItem(int position, Cursor cursor) {
            int week = weeks[selectedWeekPosition];
            int day = cursor.getInt(0);

            switch (scheduleType) {
                case Type.BY_GROUP:
                    return ScheduleFragment.newInstance(
                            position,
                            week == getCurrentWeekOfYear() && day == getCurrentDayOfWeek(),
                            typeFilterId,
                            subgroupId,
                            getStartOfWeekMillis(week),
                            day,
                            periodicity.getPeriodicity(week)
                    );
                case Type.BY_LECTURER:
                    return ScheduleFragment.newInstance(
                            position,
                            week == getCurrentWeekOfYear() && day == getCurrentDayOfWeek(),
                            typeFilterId,
                            getStartOfWeekMillis(week),
                            day,
                            periodicity.getPeriodicity(week)
                    );
                default:
                    throw new IllegalArgumentException(scheduleType + " unknown schedule type");
            }
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

        public int findCurrentDayPosition() {
            return findCurrentDayPosition(false, false);
        }

        public int findCurrentDayPosition(boolean switchDay, boolean switchWeek) {
            if (getCursor() != null) {
                return findCurrentDayPosition(getCursor(), switchDay, switchWeek);
            }

            return DAY_POSITION_UNDEFINED;
        }

        public int findCurrentDayPosition(Cursor cursor, boolean switchDay, boolean switchWeek) {
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
                    * If the time of the current day higher than the time of the last schedule
                    * item of the day
                    * */
                        if (switchDay && getCurrentTimeOfDayMillis() > cursor.getLong(1)) {
                        /*
                        * If the day is the last day in the schedule, return undefined day position.
                        * */
                            if (switchWeek && position == cursor.getCount() - 1) {
                                return DAY_POSITION_NON_THIS_WEEK;
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

                // If current day higher than the last day of the week and request allow to switch
                // week.
                if (currentDay > day && switchWeek) {
                    return DAY_POSITION_NON_THIS_WEEK;
                }
            }

            // Return undefined day position.
            return DAY_POSITION_UNDEFINED;
        }
    }
}
