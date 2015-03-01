package ua.zp.rozklad.app.processor;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;

import java.util.ArrayList;

/**
 * @author Vojko Vladimir
 */
public abstract class Processor<T> {

    protected Context mContext;
    protected ContentResolver mContentResolver;

    public Processor(Context context) {
        mContext = context;
        mContentResolver = context.getContentResolver();
    }

    public abstract void process(ArrayList<T> t);

    protected abstract ContentValues buildValuesForInsert(T t);

    protected abstract ContentValues buildValuesForUpdate(T t);
}
