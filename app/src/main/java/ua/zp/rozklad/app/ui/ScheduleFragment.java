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

import ua.zp.rozklad.app.R;
import ua.zp.rozklad.app.adapter.CursorRecyclerViewAdapter;

import static ua.zp.rozklad.app.provider.ScheduleContract.FullSchedule;

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

    private int scheduleType;
    private int typeFilterId;
    private int periodicity;
    private int dayOfWeek;

    private int subgroupId;

    private OnScheduleItemClickListener mListener;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter = null;
    private RecyclerView.LayoutManager layoutManager;

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
        recyclerView =
                (RecyclerView) inflater.inflate(R.layout.fragment_schedule, container, false);
        recyclerView.setHasFixedSize(true);

        recyclerView.setLayoutManager(layoutManager);

        getLoaderManager().initLoader(0, null, this);

        return recyclerView;
    }

    public void onScheduleItemClick(int scheduleItemId) {
        if (mListener != null) {
            mListener.onScheduleItemClicked(scheduleItemId);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        layoutManager = new LinearLayoutManager(activity);
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
        layoutManager = null;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new ScheduleCursorLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter = new ScheduleItemAdapter(getActivity(), data);
        recyclerView.setAdapter(adapter);
        recyclerView.invalidate();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    /**
     * Interface definition for a callback to be invoked when a schedule item is clicked.
     */
    public interface OnScheduleItemClickListener {
        public void onScheduleItemClicked(int scheduleItemId);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View status;
        public TextView subject;
        public TextView lecturerName;
        public TextView location;
        public TextView startTime;
        public TextView endTime;

        public ViewHolder(View view) {
            super(view);
            status = view.findViewById(R.id.status);
            subject = (TextView) view.findViewById(R.id.subject);
            lecturerName = (TextView) view.findViewById(R.id.lecturer_name);
            location = (TextView) view.findViewById(R.id.location);
        }

        public void updateFromCursor(Cursor cursor) {
            subject.setText(cursor.getString(FullSchedule.SUMMARY.COLUMN.SUBJECT_NAME));
            lecturerName.setText(cursor.getString(FullSchedule.SUMMARY.COLUMN.LECTURER_NAME));
            location.setText(
                    String.format("Аудиторія %d (%d корпус)",
                            cursor.getInt(FullSchedule.SUMMARY.COLUMN.AUDIENCE_NUMBER),
                            cursor.getInt(FullSchedule.SUMMARY.COLUMN.CAMPUS_NAME))
            );
        }
    }

    public class ScheduleItemAdapter extends CursorRecyclerViewAdapter<ViewHolder> {

        public ScheduleItemAdapter(Context context, Cursor cursor) {
            super(context, cursor);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, Cursor cursor) {
            viewHolder.updateFromCursor(cursor);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.schedule_list_item, parent, false);
            return new ViewHolder(view);
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
