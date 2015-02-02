package ua.zp.rozklad.app.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Calendar;

import ua.zp.rozklad.app.R;
import ua.zp.rozklad.app.adapter.CursorRecyclerViewAdapter;

import static java.lang.String.format;
import static ua.zp.rozklad.app.provider.ScheduleContract.FullSchedule;
import static ua.zp.rozklad.app.provider.ScheduleContract.FullSchedule.SUMMARY.COLUMN;

/**
 * {@link Fragment} that displays the schedule with the specified filter criteria.
 * Activities that contain this fragment must implement the
 * {@link ua.zp.rozklad.app.ui.ScheduleFragment.OnScheduleItemClickListener} interface
 * to handle interaction events.
 * Use the {@link ScheduleFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ScheduleFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String ARG_SCHEDULE_TYPE = "scheduleType";
    private static final String ARG_TYPE_FILTER_ID = "typeFilterId";
    private static final String ARG_SUBGROUP_ID = "subgroupId";
    private static final String ARG_PERIODICITY = "periodicity";
    private static final String ARG_DAY_OF_WEEK = "dayOfWeek";

    private static final int BY_GROUP = 0;
    private static final int BY_LECTURER = 1;

    private boolean isToday;
    private int scheduleType;
    private int typeFilterId;
    private int periodicity;
    private int dayOfWeek;

    private int subgroupId;

    private OnScheduleItemClickListener mListener;

    private RecyclerView recyclerView;
    private CursorRecyclerViewAdapter adapter;

    public static ScheduleFragment newInstance(int groupId, int periodicity, int dayOfWeek,
                                               int subgroupId) {
        ScheduleFragment fragment = new ScheduleFragment();
        Bundle args = new Bundle();

        args.putInt(ARG_SCHEDULE_TYPE, BY_GROUP);
        args.putInt(ARG_TYPE_FILTER_ID, groupId);
        args.putInt(ARG_PERIODICITY, periodicity);
        args.putInt(ARG_DAY_OF_WEEK, dayOfWeek);
        args.putInt(ARG_SUBGROUP_ID, subgroupId);

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
            // for debug
            isToday = true;

            scheduleType = args.getInt(ARG_SCHEDULE_TYPE);
            typeFilterId = args.getInt(ARG_TYPE_FILTER_ID);
            periodicity = args.getInt(ARG_PERIODICITY, -1);
            dayOfWeek = args.getInt(ARG_DAY_OF_WEEK, -1);

            subgroupId = args.getInt(ARG_SUBGROUP_ID, -1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        adapter = new ScheduleItemAdapter(getActivity(), null);

        recyclerView =
                (RecyclerView) inflater.inflate(R.layout.fragment_schedule, container, false);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        return recyclerView;
    }

    @Override
    public void onStart() {
        super.onStart();
        getLoaderManager().initLoader(0, null, this);
    }

    public void onScheduleItemClick(long scheduleItemId) {
        if (mListener != null) {
            mListener.onScheduleItemClicked(scheduleItemId);
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
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new ScheduleCursorLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private static final String INFO_FORMAT_FULL = "%s\n%d (%s)";
        private static final String INFO_FORMAT_SHORT = "%s\n%s";

        private static final int SECOND = 1000;
        private static final int MINUTE = 60 * SECOND;
        private static final int HOUR = 60 * MINUTE;
        private static final int HALF_OF_DAY = 12 * HOUR;

        public View status;
        public TextView subject;
        public TextView info;
        public TextView startTime;
        public TextView endTime;

        public ViewHolder(View view) {
            super(view);
            status = view.findViewById(R.id.status);
            subject = (TextView) view.findViewById(R.id.subject);
            info = (TextView) view.findViewById(R.id.info);
            startTime = (TextView) view.findViewById(R.id.start_time);
            endTime = (TextView) view.findViewById(R.id.end_time);
        }

        public void updateFromCursor(boolean isToday, Cursor cursor) {
            int audienceNumber = cursor.getInt(COLUMN.AUDIENCE_NUMBER);
            long startAt = cursor.getInt(COLUMN.ACADEMIC_HOUR_STAT_TIME);
            long endAt = cursor.getInt(COLUMN.ACADEMIC_HOUR_END_TIME);
            if (isToday) {
                if (inRange(startAt, endAt)) {
                    status.setVisibility(View.VISIBLE);
                } else {
                    status.setVisibility(View.GONE);
                }
            }
            subject.setText(cursor.getString(COLUMN.SUBJECT_NAME));
            info.setText(
                    (audienceNumber > 0) ?
                            format(INFO_FORMAT_FULL,
                                    cursor.getString(COLUMN.LECTURER_NAME),
                                    audienceNumber,
                                    cursor.getString(COLUMN.CAMPUS_NAME)) :
                            format(INFO_FORMAT_SHORT,
                                    cursor.getString(COLUMN.LECTURER_NAME),
                                    cursor.getInt(COLUMN.CAMPUS_NAME))
            );
            startTime.setText(makeTime(startAt));
            endTime.setText(makeTime(endAt));
        }

        private static boolean inRange(long startAt, long endAt) {
            Calendar calendarNow = Calendar.getInstance();
            long timeNow = calendarNow.get(Calendar.AM_PM) * HALF_OF_DAY
                    + calendarNow.get(Calendar.HOUR) * HOUR +
                    calendarNow.get(Calendar.MINUTE) * MINUTE;
            return timeNow >= startAt && timeNow <= endAt;
        }

        private static String makeTime(long timeToMake) {
            return makeTime(timeToMake, ":");
        }

        private static String makeTime(long timeToMake, String divider) {
            int hours = (int) (timeToMake / HOUR);
            int minutes = (int) (timeToMake % HOUR / MINUTE);
            return format("%02d%s%02d", hours, divider, minutes);
        }
    }

    public class ScheduleItemAdapter extends CursorRecyclerViewAdapter<ViewHolder>
            implements View.OnClickListener {

        public ScheduleItemAdapter(Context context, Cursor cursor) {
            super(context, cursor);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, final Cursor cursor) {
            viewHolder.updateFromCursor(isToday, cursor);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.schedule_list_item, parent, false);
            view.setOnClickListener(this);
            return new ViewHolder(view);
        }

        @Override
        public void onClick(View v) {
            onScheduleItemClick(getItemId(recyclerView.getChildPosition(v)));
        }
    }

    public static class ScheduleCursorLoader extends CursorLoader {

        public ScheduleCursorLoader(Context context) {
            super(context);
        }

        @Override
        public Cursor loadInBackground() {
            return getContext().getContentResolver()
                    .query(FullSchedule.CONTENT_URI,
                            FullSchedule.SUMMARY.PROJECTION,
                            null,
                            null,
                            FullSchedule.SUMMARY.SORT_ORDER);
        }
    }
}
