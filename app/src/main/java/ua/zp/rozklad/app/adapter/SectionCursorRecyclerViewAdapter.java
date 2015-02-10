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
     * Swap the cursor and create section items.
     */

    @Override
    public Cursor swapCursor(Cursor newCursor) {
        if (newCursor == null) {
            sections = null;
        } else {
            sections = createSections(newCursor);
        }
        return super.swapCursor(newCursor);
    }

    protected abstract HashMap<Integer, S> createSections(Cursor cursor);
}
