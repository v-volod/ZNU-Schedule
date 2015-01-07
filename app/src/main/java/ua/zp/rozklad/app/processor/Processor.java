package ua.zp.rozklad.app.processor;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Vojko Vladimir
 */
public abstract class Processor <T> {

    protected Context context;

    public Processor(Context context) {
        this.context = context;
    }

    public abstract void insert(JSONObject response) throws JSONException;

    public abstract void update(JSONObject response) throws JSONException;
}
