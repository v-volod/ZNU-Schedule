package ua.zp.rozklad.app.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.melnykov.fab.FloatingActionButton;

import java.util.Calendar;
import java.util.HashMap;

import ua.zp.rozklad.app.R;
import ua.zp.rozklad.app.adapter.SectionCursorRecyclerViewAdapter;
import ua.zp.rozklad.app.model.ScheduleItem;

import static java.lang.String.format;
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

    private static final String ARG_SCHEDULE_TYPE = "scheduleType";
    private static final String ARG_TYPE_FILTER_ID = "typeFilterId";
    private static final String ARG_SUBGROUP_ID = "subgroupId";
    private static final String ARG_START_OF_WEEK = "startOfWeek";
    private static final String ARG_DAY_OF_WEEK = "dayOfWeek";
    private static final String ARG_PERIODICITY = "periodicity";
    private static final String ARG_IS_TODAY = "isToday";

    private static final String DATE_FORMAT = "%2d %s";

    private static final int BY_GROUP = 0;
    private static final int BY_LECTURER = 1;

    private boolean isToday;
    private long startOfWeek;
    private int scheduleType;
    private int typeFilterId;
    private int periodicity;
    private int dayOfWeek;

    private int subgroupId;

    private OnScheduleItemClickListener mListener;

    private RecyclerView recyclerView;
    private ScheduleItemAdapter adapter;

    private boolean isViewCreated = false;
    private Runnable runFab;
    private Handler handler = new Handler();

    public static ScheduleFragment newInstance(boolean isToday, int groupId, int subgroupId,
                                               long startOfWeek, int dayOfWeek, int periodicity) {
        ScheduleFragment fragment = new ScheduleFragment();
        Bundle args = new Bundle();

        args.putInt(ARG_SCHEDULE_TYPE, BY_GROUP);
        args.putInt(ARG_TYPE_FILTER_ID, groupId);
        args.putInt(ARG_SUBGROUP_ID, subgroupId);
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
            scheduleType = args.getInt(ARG_SCHEDULE_TYPE);
            typeFilterId = args.getInt(ARG_TYPE_FILTER_ID);
            periodicity = args.getInt(ARG_PERIODICITY, -1);
            dayOfWeek = args.getInt(ARG_DAY_OF_WEEK, -1);
            startOfWeek = args.getLong(ARG_START_OF_WEEK, -1);
            subgroupId = args.getInt(ARG_SUBGROUP_ID, -1);
            isToday = args.getBoolean(ARG_IS_TODAY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        adapter = new ScheduleItemAdapter(getActivity());

        recyclerView =
                (RecyclerView) inflater.inflate(R.layout.fragment_schedule, container, false);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        if (runFab != null) {
            handler.post(runFab);
        }
        isViewCreated = true;

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
        CursorLoader loader = new CursorLoader(getActivity());

        loader.setUri(FullSchedule.CONTENT_URI);
        loader.setProjection(Summary.PROJECTION);
        loader.setSelection(combineSelection(
                Selection.GROUP,
                Selection.SUBGROUP,
                Selection.DAY_OF_WEEK,
                Selection.PERIODICITY
        ));
        loader.setSelectionArgs(new String[]{
                valueOf(typeFilterId), valueOf(subgroupId), valueOf(dayOfWeek), valueOf(periodicity)
        });
        loader.setSortOrder(combineSortOrder(SortOrder.DAY_OF_WEEK, SortOrder.ACADEMIC_HOUR_NUM));

        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(startOfWeek);
        calendar.add(Calendar.DAY_OF_WEEK, dayOfWeek);

        int day = calendar.get(Calendar.DAY_OF_MONTH);
        String month = getResources()
                .getStringArray(R.array.months)[calendar.get(Calendar.MONTH)];

        HashMap<Integer, String> sections = new HashMap<>();
        sections.put(0, format(DATE_FORMAT, day, month));
        adapter.setSections(sections);
        adapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public void attachFAB(final FloatingActionButton fab) {
        runFab = new Runnable() {
            @Override
            public void run() {
                fab.attachToRecyclerView(recyclerView);
            }
        };
        if (isViewCreated) {
            handler.post(runFab);
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
            text = (TextView) itemView.findViewById(R.id.sub_header_text);
        }

        public void update(String section, int color) {
            text.setTextColor(color);
            text.setText(section);
        }

    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {

        public View status;
        public TextView subject;
        public TextView info;
        public TextView startTime;
        public TextView endTime;

        public ItemViewHolder(View view) {
            super(view);
            status = view.findViewById(R.id.status);
            subject = (TextView) view.findViewById(R.id.subject);
            info = (TextView) view.findViewById(R.id.info);
            startTime = (TextView) view.findViewById(R.id.start_time);
            endTime = (TextView) view.findViewById(R.id.end_time);
        }

        public void update(boolean isToday, ScheduleItem item) {
            if (isToday) {
                if (item.isNow()) {
                    status.setVisibility(View.VISIBLE);
                } else {
                    status.setVisibility(View.GONE);
                }
            }
            subject.setText(item.getSubject());
            info.setText(item.getInfo());
            startTime.setText(item.getStartTime());
            endTime.setText(item.getEndTime());
        }
    }

    public class ScheduleItemAdapter extends SectionCursorRecyclerViewAdapter<String>
            implements View.OnClickListener {

        public ScheduleItemAdapter(Context context) {
            super(context, null);
        }

        public ScheduleItemAdapter(Context context, Cursor cursor) {
            super(context, cursor);
        }

        public ScheduleItemAdapter(Context context, Cursor cursor,
                                   HashMap<Integer, String> sections) {
            super(context, cursor, sections);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final Cursor cursor) {
            ((ItemViewHolder) viewHolder).update(isToday, new ScheduleItem(cursor));
        }

        @Override
        public void onBindSectionViewHolder(RecyclerView.ViewHolder viewHolder, String section) {
            ((SectionViewHolder) viewHolder).update(
                    section,
                    (isToday) ?
                    getResources().getColor(R.color.colorPrimary) :
                    getResources().getColor(R.color.sub_header_text_color)
            );
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            switch (viewType) {
                case TYPE_SECTION: {
                    View view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.list_sub_header, parent, false);
                    return new SectionViewHolder(view);
                }
                case TYPE_ITEM: {
                    View view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.schedule_list_item, parent, false);
                    view.setOnClickListener(this);
                    return new ItemViewHolder(view);
                }
                default:
                    throw new IllegalArgumentException("There is no type that matches the type: " +
                            viewType);
            }
        }

        @Override
        public void onClick(View v) {
            onScheduleItemClick(getItemId(recyclerView.getChildPosition(v)));
        }
    }
}
