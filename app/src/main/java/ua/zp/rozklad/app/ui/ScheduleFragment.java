package ua.zp.rozklad.app.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.melnykov.fab.FloatingActionButton;

import java.util.Calendar;
import java.util.HashMap;

import ua.zp.rozklad.app.App;
import ua.zp.rozklad.app.R;
import ua.zp.rozklad.app.adapter.SectionCursorRecyclerViewAdapter;
import ua.zp.rozklad.app.util.CalendarUtils;

import static java.lang.String.valueOf;
import static ua.zp.rozklad.app.provider.ScheduleContract.FullSchedule;
import static ua.zp.rozklad.app.provider.ScheduleContract.FullSchedule.Summary;
import static ua.zp.rozklad.app.provider.ScheduleContract.FullSchedule.Summary.Selection;
import static ua.zp.rozklad.app.provider.ScheduleContract.FullSchedule.Summary.SortOrder;
import static ua.zp.rozklad.app.provider.ScheduleContract.combineSelection;
import static ua.zp.rozklad.app.provider.ScheduleContract.combineSortOrder;

/**
 * {@link Fragment} that displays the schedule with the specified filter criteria.
 * Activities that contain this fragment must implement the
 * {@link ua.zp.rozklad.app.ui.ScheduleFragment.OnScheduleItemClickListener} interface
 * to handle interaction events.
 * Use the {@link ScheduleFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ScheduleFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String ARG_POSITION = "position";
    private static final String ARG_SCHEDULE_TYPE = "scheduleType";
    private static final String ARG_TYPE_FILTER_ID = "typeFilterId";
    private static final String ARG_SUBGROUP_ID = "subgroupId";
    private static final String ARG_START_OF_WEEK = "startOfWeek";
    private static final String ARG_DAY_OF_WEEK = "dayOfWeek";
    private static final String ARG_PERIODICITY = "periodicity";
    private static final String ARG_IS_TODAY = "isToday";

    public static interface Type {
        int BY_GROUP = 0;
        int BY_LECTURER = 1;
    }

    private int position;
    private boolean isToday;
    private long startOfWeek;
    private int scheduleType;
    private long typeFilterId;
    private int periodicity;
    private int dayOfWeek;
    private int subgroupId;

    private OnScheduleItemClickListener mListener;

    private RecyclerView mRecyclerView;
    private ScheduleItemAdapter mAdapter;

    private boolean isAttached = false;
    private boolean isViewCreated = false;
    private Runnable runFab;
    private Handler handler = new Handler();

    public static ScheduleFragment newInstance(int position, boolean isToday, long groupId,
                                               int subgroupId, long startOfWeek, int dayOfWeek,
                                               int periodicity) {
        ScheduleFragment fragment = new ScheduleFragment();
        Bundle args = new Bundle();

        args.putInt(ARG_POSITION, position);
        args.putInt(ARG_SCHEDULE_TYPE, Type.BY_GROUP);
        args.putLong(ARG_TYPE_FILTER_ID, groupId);
        args.putInt(ARG_SUBGROUP_ID, subgroupId);
        args.putLong(ARG_START_OF_WEEK, startOfWeek);
        args.putInt(ARG_DAY_OF_WEEK, dayOfWeek);
        args.putInt(ARG_PERIODICITY, periodicity);
        args.putBoolean(ARG_IS_TODAY, isToday);

        fragment.setArguments(args);
        return fragment;
    }

    public static ScheduleFragment newInstance(int position, boolean isToday, long lecturerId,
                                               long startOfWeek, int dayOfWeek, int periodicity) {
        ScheduleFragment fragment = new ScheduleFragment();
        Bundle args = new Bundle();

        args.putInt(ARG_POSITION, position);
        args.putInt(ARG_SCHEDULE_TYPE, Type.BY_LECTURER);
        args.putLong(ARG_TYPE_FILTER_ID, lecturerId);
        args.putLong(ARG_START_OF_WEEK, startOfWeek);
        args.putInt(ARG_DAY_OF_WEEK, dayOfWeek);
        args.putInt(ARG_PERIODICITY, periodicity);
        args.putBoolean(ARG_IS_TODAY, isToday);

        fragment.setArguments(args);
        return fragment;
    }

    public ScheduleFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            position = args.getInt(ARG_POSITION);
            scheduleType = args.getInt(ARG_SCHEDULE_TYPE);
            typeFilterId = args.getLong(ARG_TYPE_FILTER_ID);
            periodicity = args.getInt(ARG_PERIODICITY, -1);
            dayOfWeek = args.getInt(ARG_DAY_OF_WEEK, -1);
            startOfWeek = args.getLong(ARG_START_OF_WEEK, -1);
            subgroupId = args.getInt(ARG_SUBGROUP_ID, -1);
            isToday = args.getBoolean(ARG_IS_TODAY);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnScheduleItemClickListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnScheduleItemClickListener");
        }
        isAttached = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_schedule, container, false);

        mRecyclerView = (RecyclerView) view;
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        if (runFab != null) {
            handler.post(runFab);
        }
        isViewCreated = true;

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAdapter = new ScheduleItemAdapter(null, null);
        mRecyclerView.setAdapter(mAdapter);
        getLoaderManager().initLoader(scheduleType, null, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isToday) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        isAttached = false;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader = new CursorLoader(getActivity());

        loader.setUri(FullSchedule.CONTENT_URI);
        loader.setProjection(Summary.PROJECTION);
        switch (id) {
            case Type.BY_GROUP:
                loader.setSelection(combineSelection(
                        Selection.GROUP,
                        Selection.SUBGROUP,
                        Selection.DAY_OF_WEEK,
                        Selection.PERIODICITY
                ));
                loader.setSelectionArgs(new String[]{
                        valueOf(typeFilterId), valueOf(subgroupId), valueOf(dayOfWeek), valueOf(periodicity)
                });
                break;
            case Type.BY_LECTURER:
                loader.setSelection(combineSelection(
                        Selection.LECTURER,
                        Selection.DAY_OF_WEEK,
                        Selection.PERIODICITY
                ));
                loader.setSelectionArgs(new String[]{
                        valueOf(typeFilterId), valueOf(dayOfWeek), valueOf(periodicity)
                });
        }
        loader.setSortOrder(combineSortOrder(SortOrder.DAY_OF_WEEK, SortOrder.ACADEMIC_HOUR_NUM));

        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    public void attachFAB(final FloatingActionButton mFab) {
        runFab = new Runnable() {
            @Override
            public void run() {
                mFab.attachToRecyclerView(mRecyclerView);
                mFab.show();
            }
        };
        if (isViewCreated) {
            handler.post(runFab);
        }
    }

    public void onScheduleItemClick(long scheduleItemId) {
        if (mListener != null) {
            mListener.onScheduleItemClicked(scheduleItemId);
        }
    }

    public int getPosition() {
        return position;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public int getPeriodicity() {
        return periodicity;
    }

    public void reload(boolean isToday, long startOfWeek, int periodicity) {
        this.isToday = isToday;
        this.startOfWeek = startOfWeek;
        this.periodicity = periodicity;
        Bundle args = getArguments();
        if (args != null) {
            args.putBoolean(ARG_IS_TODAY, isToday);
            args.putLong(ARG_START_OF_WEEK, startOfWeek);
            args.putInt(ARG_PERIODICITY, periodicity);
        }
        if (isAttached) {
            getLoaderManager().restartLoader(scheduleType, null, this);
        }
    }

    /**
     * Interface definition for a callback to be invoked when a schedule item is clicked.
     */
    public interface OnScheduleItemClickListener {
        /**
         * @param scheduleItemId id of the row of the schedule table in the database.
         */
        public void onScheduleItemClicked(long scheduleItemId);
    }

    public static class SectionViewHolder extends RecyclerView.ViewHolder {

        TextView text;

        public SectionViewHolder(View itemView) {
            super(itemView);
            text = (TextView) itemView.findViewById(R.id.text);
        }
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {

        public View status;
        public TextView subjectText;
        public TextView infoText;
        public TextView startTimeText;
        public TextView endTimeText;

        public ItemViewHolder(View view) {
            super(view);
            status = view.findViewById(R.id.status);
            subjectText = (TextView) view.findViewById(R.id.subject);
            infoText = (TextView) view.findViewById(R.id.info);
            startTimeText = (TextView) view.findViewById(R.id.start_time);
            endTimeText = (TextView) view.findViewById(R.id.end_time);
        }

        public void update(boolean isToday, Cursor cursor, Resources res, int scheduleType) {
            long startTime = cursor.getLong(Summary.Column.ACADEMIC_HOUR_STAT_TIME);
            long endTime = cursor.getInt(Summary.Column.ACADEMIC_HOUR_END_TIME);
            subjectText.setText(cursor.getString(Summary.Column.SUBJECT_NAME));

            if (isToday && inRange(startTime, endTime)) {
                status.setVisibility(View.VISIBLE);
            } else {
                status.setVisibility(View.GONE);
            }

            String lecturerOrGroups = (scheduleType == Type.BY_GROUP) ?
                    cursor.getString(Summary.Column.LECTURER_NAME) :
                    cursor.getString(Summary.Column.GROUPS);
            String audience = cursor.getString(Summary.Column.AUDIENCE_NUMBER);
            String campus = cursor.getString(Summary.Column.CAMPUS_NAME);
            String location = audience +
                    ((TextUtils.isEmpty(campus)) ? "" :
                            (TextUtils.isEmpty(audience) ? "(" : " (") + campus + ")");
            int type = cursor.getInt(Summary.Column.CLASS_TYPE);

            String info = lecturerOrGroups + "\n" +
                    ((TextUtils.isEmpty(location)) ? "" : location) +
                    ((type == 0) ? "" :
                            ((TextUtils.isEmpty(location) ? "" : ", ") +
                                    res.getStringArray(R.array.class_type)[type]));

            infoText.setText(info);

            startTimeText.setText(CalendarUtils.makeTime(startTime));
            endTimeText.setText(CalendarUtils.makeTime(endTime));
        }

        private boolean inRange(long startAt, long endAt) {
            Calendar calendarNow = Calendar.getInstance();
            long timeNow = calendarNow.get(Calendar.AM_PM) * CalendarUtils.HALF_DAY_TIME_STAMP
                    + calendarNow.get(Calendar.HOUR) * CalendarUtils.HOUR_TIME_STAMP +
                    calendarNow.get(Calendar.MINUTE) * CalendarUtils.MINUTE_TIME_STAMP;
            return timeNow >= startAt && timeNow <= endAt;
        }
    }


    public class ScheduleItemAdapter extends SectionCursorRecyclerViewAdapter<String>
            implements View.OnClickListener {
        private final String[] MONTHS = getResources().getStringArray(R.array.months);
        private final int COLOR_DEFAULT = getResources().getColor(R.color.sub_header_text_color);
        private final int COLOR_TODAY = getResources().getColor(R.color.blue_500);

        public ScheduleItemAdapter(HashMap<Integer, String> sections, Cursor cursor) {
            super(sections, cursor);
        }

        @Override
        public void onBindItemViewHolder(RecyclerView.ViewHolder holder, Cursor cursor) {
            ((ItemViewHolder) holder).update(isToday,cursor, getResources(), scheduleType);
        }

        @Override
        public void onBindSectionViewHolder(RecyclerView.ViewHolder holder, String section) {
            SectionViewHolder sectionViewHolder = (SectionViewHolder) holder;
            sectionViewHolder.text.setText(section);
            sectionViewHolder.text.setTextColor((isToday) ? COLOR_TODAY: COLOR_DEFAULT);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            switch (viewType) {
                case TYPE_SECTION: {
                    View view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.sub_header_with_divider, parent, false);
                    return new SectionViewHolder(view);
                }
                case TYPE_ITEM: {
                    View view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.schedule_list_item, parent, false);
                    view.setOnClickListener(this);
                    return new ItemViewHolder(view);
                }
                default:
                    throw new IllegalArgumentException("illegal type of the view " + viewType);
            }
        }

        @Override
        protected HashMap<Integer, String> createSections(Cursor cursor) {
            HashMap<Integer, String> sections = new HashMap<>();
            sections.put(0, getDate());

            return sections;
        }

        private String getDate() {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(CalendarUtils.addDays(startOfWeek, dayOfWeek));

            return calendar.get(Calendar.DAY_OF_MONTH) + " " + MONTHS[calendar.get(Calendar.MONTH)];
        }

        @Override
        public void onClick(View v) {
            onScheduleItemClick(getItemId(mRecyclerView.getChildPosition(v)));
        }
    }
}
