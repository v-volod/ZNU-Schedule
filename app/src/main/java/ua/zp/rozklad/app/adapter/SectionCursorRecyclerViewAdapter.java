package ua.zp.rozklad.app.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;

import java.util.HashMap;

/**
 * @author Vojko Vladimir
 */
public abstract class SectionCursorRecyclerViewAdapter<S>
        extends CursorRecyclerViewAdapter<RecyclerView.ViewHolder> {
    protected static final int TYPE_SECTION = 0;
    protected static final int TYPE_ITEM = 1;

    private HashMap<Integer, S> sections;

    public SectionCursorRecyclerViewAdapter(Context context, Cursor cursor) {
        this(context, cursor, null);
    }

    public SectionCursorRecyclerViewAdapter(Context context, Cursor cursor,
                                            HashMap<Integer, S> sections) {
        super(context, cursor);
        this.sections = sections;
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

    public void setSections(HashMap<Integer, S> sections) {
        this.sections = sections;
        notifyDataSetChanged();
    }

    protected S getSectionItem(int position) {
        return sections.get(position);
    }

    private boolean isSection(int position) {
        return sections != null && sections.containsKey(position);
    }

    private int getSectionItemCount() {
        if (sections != null) {
            return sections.size();
        }
        return 0;
    }

    /**
     * Swap the cursor and set new section items. Called if sections is dependent on the data.
     */
    public Cursor swapCursor(Cursor newCursor, HashMap<Integer, S> newSections) {
        sections = newSections;
        return super.swapCursor(newCursor);
    }
}
