package ua.zp.rozklad.app.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;

import java.util.HashMap;
import java.util.Set;

import ua.zp.rozklad.app.App;

/**
 * @author Vojko Vladimir
 */
public abstract class SectionCursorRecyclerViewAdapter<S>
        extends CursorRecyclerViewAdapter<RecyclerView.ViewHolder> {
    protected static final int TYPE_SECTION = 0;
    protected static final int TYPE_ITEM = 1;

    private HashMap<Integer, S> mSections;

    /*
    * Unusable yet. Continue development...
    * */
    private SectionCursorRecyclerViewAdapter(Context context, Cursor cursor) {
        super(context, cursor);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (isSection(position)) {
            onBindSectionViewHolder(viewHolder, getSectionItem(position));
        } else {
            super.onBindViewHolder(viewHolder, getPosition(position));
        }
    }

    public abstract void onBindSectionViewHolder(RecyclerView.ViewHolder viewHolder, S section);

    @Override
    public long getItemId(int position) {
        if (isSection(position)) {
            return 0;
        }
        return super.getItemId(getPosition(position));
    }

    private int getPosition(int position) {

        for (int i = 0; i < position; i++) {
            if (isSection(i)) {
                position--;
            }
        }

        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return (isSection(position)) ? TYPE_SECTION : TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        return super.getItemCount() + getSectionItemCount();
    }

    protected S getSectionItem(int position) {
        return mSections.get(position);
    }

    private boolean isSection(int position) {
        return mSections != null && mSections.containsKey(position);
    }

    private int getSectionItemCount() {
        if (mSections != null) {
            return mSections.size();
        }
        return 0;
    }

    /**
     * Swap the cursor and create section items.
     */

    @Override
    public Cursor swapCursor(Cursor newCursor) {
        Cursor oldCursor = super.swapCursor(newCursor);

        if (newCursor == null) {
            for (int position : mSections.keySet()) {
                notifyItemRemoved(position);
            }
            mSections = null;
        } else {
            HashMap<Integer, S> oldSections = mSections;
            mSections = createSections(newCursor);

            if (oldSections == null || oldSections.size() == 0) {
                for (int position : mSections.keySet()) {
                    notifyItemInserted(position);
                }
            } else {
                Set<Integer> set = oldSections.keySet();
                if (set.retainAll(mSections.keySet())) {
                    for (int position : set) {
                        notifyItemChanged(position);
                    }
                }

                set = oldSections.keySet();
                if (set.removeAll(mSections.keySet())) {
                    for (int position : set) {
                        notifyItemRemoved(position);
                    }
                }

                set = mSections.keySet();
                if (set.removeAll(oldSections.keySet())) {
                    for (int position : set) {
                        notifyItemInserted(position);
                    }
                }
            }

            App.LOG_D("Sections swaped");
        }

        return oldCursor;
    }

    protected abstract HashMap<Integer, S> createSections(Cursor cursor);
//
//    @Override
//    protected void onItemChanged(int position) {
//        super.onItemChanged(getSectionsBeforeCount(position) + position);
//    }
//
//    @Override
//    protected void onItemInserted(int position) {
//        super.onItemInserted(getSectionsBeforeCount(position) + position);
//    }
//
//    @Override
//    protected void onItemRemoved(int position) {
//        super.onItemRemoved(getSectionsBeforeCount(position) + position);
//    }
//
//    protected int getSectionsBeforeCount(int position) {
//        int count = 0;
//
//        for (int i = 0; i < position; i++) {
//            if (isSection(i)) {
//                count++;
//            }
//        }
//
//        return count;
//    }
}
