package ua.zp.rozklad.app.processor;

import android.content.Context;

/**
 * @author Vojko Vladimir
 */
public abstract class Processor<T> {

    protected Context context;

    public Processor(Context context) {
        this.context = context;
    }
}
