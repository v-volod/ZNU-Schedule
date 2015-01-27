package ua.zp.rozklad.app.processor;

import android.content.ContentValues;
import android.content.Context;

import java.util.ArrayList;

/**
 * @author Vojko Vladimir
 */
public abstract class Processor<T, D> {

    protected Context context;

    public Processor(Context context) {
        this.context = context;
    }

    public abstract D process(ArrayList<T> t);

    protected abstract ContentValues buildValuesForInsert(T t);

    protected abstract ContentValues buildValuesForUpdate(T t);
}
