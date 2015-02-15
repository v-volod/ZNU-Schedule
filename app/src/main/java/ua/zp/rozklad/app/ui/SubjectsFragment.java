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
import ua.zp.rozklad.app.provider.ScheduleContract.FullSchedule.Summary;
import ua.zp.rozklad.app.provider.ScheduleContract.FullSubject;

import static ua.zp.rozklad.app.provider.ScheduleContract.combineArgs;
import static ua.zp.rozklad.app.provider.ScheduleContract.combineSelection;
import static ua.zp.rozklad.app.provider.ScheduleContract.groupBySelection;

public class SubjectsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String ARG_GROUP_ID = "groupId";
    private static final String ARG_SUBGROUP = "subgroup";

    private OnSubjectClickListener mListener;

    private int groupId;
    private int subgroup;

    private RecyclerView mRecyclerView;
    private SubjectsAdapter mAdapter;

    public static SubjectsFragment newInstance(int groupId, int subgroup) {
        SubjectsFragment fragment = new SubjectsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_GROUP_ID, groupId);
        args.putInt(ARG_SUBGROUP, subgroup);
        fragment.setArguments(args);
        return fragment;
    }

    public SubjectsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            groupId = getArguments().getInt(ARG_GROUP_ID);
            subgroup = getArguments().getInt(ARG_SUBGROUP);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnSubjectClickListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnSubjectClickListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_subjects, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAdapter = new SubjectsAdapter(getActivity(), null);
        mRecyclerView.setAdapter(mAdapter);

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader = new CursorLoader(getActivity());

        loader.setUri(FullSubject.CONTENT_URI);
        loader.setProjection(FullSubject.PROJECTION);
        loader.setSelection(combineSelection(Summary.Selection.GROUP, Summary.Selection.SUBGROUP) +
                groupBySelection(FullSubject._ID));
        loader.setSelectionArgs(combineArgs(groupId, subgroup));
        loader.setSortOrder(FullSubject.DEFAULT_SORT_ORDER);

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

    public void onSubjectClicked(long subjectId) {
        if (mListener != null) {
            mListener.onSubjectClicked(subjectId);
        }
    }

    public void reload(int groupId, int subgroup) {
        this.groupId = groupId;
        this.subgroup = subgroup;
        if (getArguments() != null) {
            getArguments().putInt(ARG_GROUP_ID, groupId);
            getArguments().putInt(ARG_SUBGROUP, subgroup);
            getLoaderManager().restartLoader(0, null, this);
        }
    }

    /**
     * Interface definition for a callback to be invoked when a subject is clicked.
     */
    public interface OnSubjectClickListener {
        /**
         * @param subjectId id of the row of the subject table in the database.
         */
        public void onSubjectClicked(long subjectId);
    }

    public static class SubjectVH extends RecyclerView.ViewHolder {

        TextView subjectName;
        TextView lecturers;

        public SubjectVH(View itemView) {
            super(itemView);
            subjectName = (TextView) itemView.findViewById(R.id.primary_text);
            lecturers = (TextView) itemView.findViewById(R.id.secondary_text);
        }

        public void update(Cursor cursor) {
            subjectName.setText(cursor.getString(FullSubject.Column.SUBJECT_NAME));
            String lecturersText = cursor.getString(FullSubject.Column.LECTURERS);
            if (lecturersText == null) {
                lecturers.setText("");
            } else {
                lecturers.setText(lecturersText.replace(",", ", "));
            }
        }
    }

    private class SubjectsAdapter extends CursorRecyclerViewAdapter<SubjectVH>
            implements View.OnClickListener {

        public SubjectsAdapter(Context context, Cursor cursor) {
            super(context, cursor);
        }

        @Override
        public void onBindViewHolder(SubjectVH viewHolder, Cursor cursor) {
            viewHolder.update(cursor);
        }

        @Override
        public SubjectVH onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater
                    .from(getActivity()).inflate(R.layout.two_line_item, parent, false);
            view.setOnClickListener(this);
            return new SubjectVH(view);
        }

        @Override
        public void onClick(View v) {
            onSubjectClicked(getItemId(mRecyclerView.getChildPosition(v)));
        }
    }
}
