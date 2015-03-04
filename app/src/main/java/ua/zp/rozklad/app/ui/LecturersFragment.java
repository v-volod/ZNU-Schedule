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
import ua.zp.rozklad.app.account.GroupAccount;
import ua.zp.rozklad.app.adapter.CursorRecyclerViewAdapter;
import ua.zp.rozklad.app.provider.ScheduleContract.FullLecturer;
import ua.zp.rozklad.app.provider.ScheduleContract.FullSchedule.Summary;

import static ua.zp.rozklad.app.provider.ScheduleContract.combineArgs;
import static ua.zp.rozklad.app.provider.ScheduleContract.combineSelection;
import static ua.zp.rozklad.app.provider.ScheduleContract.groupBySelection;

public class LecturersFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String ARG_GROUP_ID = "groupId";
    private static final String ARG_SUBGROUP = "subgroup";

    private OnLecturerClickListener mListener;

    private int groupId;
    private int subgroup;

    private RecyclerView mRecyclerView;
    private LecturersAdapter mAdapter;

    public static LecturersFragment newInstance(GroupAccount groupAccount) {
        LecturersFragment fragment = new LecturersFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_GROUP_ID, groupAccount.getGroupId());
        args.putInt(ARG_SUBGROUP, groupAccount.getSubgroup());
        fragment.setArguments(args);
        return fragment;
    }

    public LecturersFragment() {
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
            mListener = (OnLecturerClickListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnLecturerClickListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lecturers, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAdapter = new LecturersAdapter(getActivity(), null);
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

        loader.setUri(FullLecturer.CONTENT_URI);
        loader.setProjection(FullLecturer.PROJECTION);
        loader.setSelection(combineSelection(Summary.Selection.GROUP, Summary.Selection.SUBGROUP) +
                groupBySelection(FullLecturer._ID));
        loader.setSelectionArgs(combineArgs(groupId, subgroup));
        loader.setSortOrder(FullLecturer.DEFAULT_SORT_ORDER);

        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.getCount() > 0) {
            mRecyclerView.setVisibility(View.VISIBLE);
        } else {
            mRecyclerView.setVisibility(View.INVISIBLE);
        }
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mRecyclerView.setVisibility(View.INVISIBLE);
        mAdapter.swapCursor(null);
    }

    public void reload(GroupAccount groupAccount) {
        groupId = groupAccount.getGroupId();
        subgroup = groupAccount.getSubgroup();
        if (getArguments() != null) {
            getArguments().putInt(ARG_GROUP_ID, groupId);
            getArguments().putInt(ARG_SUBGROUP, subgroup);
            getLoaderManager().restartLoader(0, null, this);
        }
    }

    public void onLecturerClicked(long lecturerId) {
        if (mListener != null) {
            mListener.onLecturerClicked(lecturerId);
        }
    }

    /**
     * Interface definition for a callback to be invoked when a lecturer is clicked.
     */
    public interface OnLecturerClickListener {
        /**
         * @param lecturerId id of the row of the lecturer table in the database.
         */
        public void onLecturerClicked(long lecturerId);
    }

    public static class LecturerVH extends RecyclerView.ViewHolder {

        TextView lecturerName;
        TextView lecturerSubjects;

        public LecturerVH(View itemView) {
            super(itemView);
            lecturerName = (TextView) itemView.findViewById(R.id.primary_text);
            lecturerSubjects = (TextView) itemView.findViewById(R.id.secondary_text);
        }

        public void update(Cursor cursor) {
            lecturerName.setText(cursor.getString(FullLecturer.Column.LECTURER_NAME));
//            String[] subjects = cursor.getString(FullLecturer.Column.SUBJECTS).split(",");
//            String subjectsText = "";
//            for (int i = 0; i < subjects.length; i++) {
//                if (subjects[i].length() > 17 && i != (subjects.length - 1)) {
//                    subjectsText += subjects[i].substring(0, 16) + "...";
//                } else {
//                    subjectsText += subjects[i];
//                }
//                if (i != subjects.length - 1) {
//                    subjectsText += ", ";
//                }
//            }
//            lecturerSubjects.setText(subjectsText);
            lecturerSubjects.setText(cursor.getString(FullLecturer.Column.SUBJECTS));
        }
    }

    private class LecturersAdapter extends CursorRecyclerViewAdapter<LecturerVH>
            implements View.OnClickListener {

        public LecturersAdapter(Context context, Cursor cursor) {
            super(context, cursor);
        }

        @Override
        public void onBindViewHolder(LecturerVH viewHolder, Cursor cursor) {
            viewHolder.update(cursor);
        }

        @Override
        public LecturerVH onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater
                    .from(getActivity()).inflate(R.layout.two_line_item, parent, false);
            view.setOnClickListener(this);
            return new LecturerVH(view);
        }

        @Override
        public void onClick(View v) {
            onLecturerClicked(getItemId(mRecyclerView.getChildPosition(v)));
        }
    }
}
